package com.ultimatejw.mjcn.ui.main.chat

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.domain.model.ChatMessage
import com.ultimatejw.mjcn.domain.model.ReferencedItem

class ChatMessageAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DiffCallback) {

    var onSuggestionClick: ((String) -> Unit)? = null

    var showLoading = false
        set(value) {
            if (field == value) return
            field = value
            val pos = super.getItemCount()
            if (value) notifyItemInserted(pos) else notifyItemRemoved(pos)
        }

    override fun getItemCount() = super.getItemCount() + if (showLoading) 1 else 0

    override fun getItemViewType(position: Int): Int {
        if (showLoading && position == super.getItemCount()) return VIEW_TYPE_LOADING
        return if (getItem(position).isFromUser) VIEW_TYPE_USER else VIEW_TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_USER    -> UserViewHolder(inflater.inflate(R.layout.item_chat_message_user, parent, false))
            VIEW_TYPE_LOADING -> LoadingViewHolder(inflater.inflate(R.layout.item_chat_loading, parent, false))
            else              -> AiViewHolder(inflater.inflate(R.layout.item_chat_message_ai, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is LoadingViewHolder) return
        val message = getItem(position)
        when (holder) {
            is UserViewHolder -> holder.bind(message.content)
            is AiViewHolder   -> holder.bind(message.content, message.referencedItems, onSuggestionClick)
        }
    }

    class UserViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        private val tvMessage: TextView = root.findViewById(R.id.tv_message)
        fun bind(content: String) { tvMessage.text = content }
    }

    class AiViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        private val containerContent: LinearLayout = root.findViewById(R.id.container_content)
        private val containerSuggestions: LinearLayout = root.findViewById(R.id.container_suggestions)
        private val containerSuggestionItems: LinearLayout = root.findViewById(R.id.container_suggestion_items)

        fun bind(content: String, referencedItems: List<ReferencedItem>, onSuggestionClick: ((String) -> Unit)?) {
            val ctx = itemView.context
            val density = ctx.resources.displayMetrics.density

            containerContent.removeAllViews()
            containerSuggestionItems.removeAllViews()

            val parsed = parseAiContent(content)

            // 본문 렌더링
            var firstLine = true
            parsed.mainBlocks.forEach { block ->
                if (!firstLine) addSpacer(containerContent, (6 * density).toInt())
                firstLine = false

                block.lines().forEach { rawLine ->
                    val line = rawLine.trimEnd()
                    if (line.isBlank()) {
                        addSpacer(containerContent, (4 * density).toInt())
                        return@forEach
                    }
                    when {
                        line.trimStart().matches(Regex("^#{1,3}\\s+.+")) -> {
                            val text = line.trimStart().replace(Regex("^#{1,3}\\s+"), "")
                            addSectionTitle(ctx, containerContent, text, density)
                        }
                        line.trimStart().matches(Regex("^\\d+\\.\\s+.+")) -> {
                            addSectionTitle(ctx, containerContent, line.trimStart(), density)
                        }
                        line.trimStart().startsWith("- ") || line.trimStart().startsWith("• ") -> {
                            val text = line.trimStart().removePrefix("- ").removePrefix("• ")
                            addBulletItem(ctx, containerContent, text, density)
                        }
                        line.trim().startsWith("**") && line.trim().endsWith("**") && line.trim().length > 4 -> {
                            val text = line.trim().removePrefix("**").removeSuffix("**").trim()
                            addBoldLine(ctx, containerContent, text, density)
                        }
                        else -> addBodyText(ctx, containerContent, line.trimStart(), density)
                    }
                }
            }

            // 추가 질문 섹션
            if (parsed.suggestions.isNotEmpty()) {
                containerSuggestions.visibility = View.VISIBLE
                parsed.suggestions.forEach { suggestion ->
                    addSuggestionItem(ctx, containerSuggestionItems, suggestion, density) {
                        onSuggestionClick?.invoke(suggestion)
                    }
                }
            } else {
                containerSuggestions.visibility = View.GONE
            }
        }

        private data class ParsedContent(
            val mainBlocks: List<String>,
            val suggestions: List<String>
        )

        private fun parseAiContent(raw: String): ParsedContent {
            // 베어 URL만 제거, [text](url) 마크다운 링크는 renderMarkdown에서 처리
            val cleaned = raw.replace(Regex("(?<!\\()https?://\\S+"), "")

            val blocks = cleaned.split(Regex("\\n{2,}"))
                .map { it.trim() }
                .filter { it.isNotBlank() }

            if (blocks.isEmpty()) return ParsedContent(emptyList(), emptyList())

            // "추가로 물을 수 있어요" 섹션 파싱
            val suggestIdx = blocks.indexOfFirst { it.trimStart().startsWith("추가로 물을 수 있어요") }

            if (suggestIdx >= 0) {
                val mainBlocks = blocks.subList(0, suggestIdx)
                val suggestions = blocks[suggestIdx].lines()
                    .drop(1)
                    .filter { it.trimStart().let { l -> l.startsWith("-") || l.startsWith("•") || l.startsWith("\"") || l.startsWith("\"") } }
                    .map { it.trimStart('-', '•', '"', '"', ' ').trimEnd('"', '"').trim() }
                    .filter { it.isNotBlank() }
                return ParsedContent(mainBlocks, suggestions)
            }

            return ParsedContent(blocks, emptyList())
        }

        private fun renderMarkdown(ctx: android.content.Context, text: String): SpannableStringBuilder {
            val sb = SpannableStringBuilder()
            // [text](url) 과 **bold** 통합 처리
            val combined = Regex("\\[([^\\]]+)]\\((https?://[^)\\s]+)\\)|\\*\\*(.+?)\\*\\*")
            var lastEnd = 0
            combined.findAll(text).forEach { match ->
                if (match.range.first > lastEnd) {
                    sb.append(text.substring(lastEnd, match.range.first))
                }
                if (match.value.startsWith("[")) {
                    val linkText = match.groupValues[1]
                    val url = match.groupValues[2]
                    val start = sb.length
                    sb.append(linkText)
                    sb.setSpan(object : android.text.style.ClickableSpan() {
                        override fun onClick(widget: View) {
                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                        override fun updateDrawState(ds: android.text.TextPaint) {
                            ds.color = ctx.getColor(R.color.primary)
                            ds.isUnderlineText = true
                        }
                    }, start, sb.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else {
                    val start = sb.length
                    sb.append(match.groupValues[3])
                    sb.setSpan(StyleSpan(Typeface.BOLD), start, sb.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                lastEnd = match.range.last + 1
            }
            if (lastEnd < text.length) sb.append(text.substring(lastEnd))
            return sb
        }

        private fun addBoldLine(ctx: android.content.Context, container: LinearLayout, text: String, density: Float) {
            val tv = TextView(ctx).apply {
                this.text = text
                textSize = 16f
                setTypeface(ResourcesCompat.getFont(ctx, R.font.pretendard_semibold), Typeface.BOLD)
                setTextColor(ctx.getColor(R.color.font_color1))
                setLineSpacing(2 * density, 1f)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = (6 * density).toInt() }
            }
            container.addView(tv)
        }

        private fun addSectionTitle(ctx: android.content.Context, container: LinearLayout, text: String, density: Float) {
            val tv = TextView(ctx).apply {
                this.text = renderMarkdown(ctx, text)
                textSize = 15f
                setTypeface(ResourcesCompat.getFont(ctx, R.font.pretendard_semibold), Typeface.BOLD)
                setTextColor(ctx.getColor(R.color.font_color1))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = (10 * density).toInt() }
            }
            container.addView(tv)
        }

        private fun addBulletItem(ctx: android.content.Context, container: LinearLayout, text: String, density: Float) {
            val tv = TextView(ctx).apply {
                this.text = renderMarkdown(ctx, "• $text")
                textSize = 14f
                typeface = ResourcesCompat.getFont(ctx, R.font.pretendard_regular)
                setTextColor(ctx.getColor(R.color.font_color1_1))
                setLineSpacing(2 * density, 1f)
                setPadding((4 * density).toInt(), 0, 0, 0)
                movementMethod = android.text.method.LinkMovementMethod.getInstance()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = (4 * density).toInt() }
            }
            container.addView(tv)
        }

        private fun addBodyText(ctx: android.content.Context, container: LinearLayout, text: String, density: Float) {
            val tv = TextView(ctx).apply {
                this.text = renderMarkdown(ctx, text)
                textSize = 14f
                typeface = ResourcesCompat.getFont(ctx, R.font.pretendard_regular)
                setTextColor(ctx.getColor(R.color.font_color1))
                setLineSpacing(2 * density, 1f)
                movementMethod = android.text.method.LinkMovementMethod.getInstance()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = (4 * density).toInt() }
            }
            container.addView(tv)
        }

        private fun addLinkButton(ctx: android.content.Context, container: LinearLayout, label: String, url: String, density: Float) {
            val btnLayout = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = ContextCompat.getDrawable(ctx, R.drawable.bg_chat_link_button)
                val pH = (16 * density).toInt()
                val pV = (12 * density).toInt()
                setPadding(pH, pV, pH, pV)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = (6 * density).toInt() }
                setOnClickListener {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
            val labelTv = TextView(ctx).apply {
                text = label
                textSize = 14f
                setTypeface(ResourcesCompat.getFont(ctx, R.font.pretendard_medium))
                setTextColor(ctx.getColor(R.color.white))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val iconView = ImageView(ctx).apply {
                setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_arrow_white_chevron))
                val size = (20 * density).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginStart = (8 * density).toInt()
                }
            }
            btnLayout.addView(labelTv)
            btnLayout.addView(iconView)
            container.addView(btnLayout)
        }

        private fun addSuggestionItem(
            ctx: android.content.Context,
            container: LinearLayout,
            text: String,
            density: Float,
            onClick: () -> Unit
        ) {
            val tv = TextView(ctx).apply {
                this.text = "\"$text\""
                textSize = 13f
                typeface = ResourcesCompat.getFont(ctx, R.font.pretendard_regular)
                setTextColor(ctx.getColor(R.color.font_color1_1))
                setLineSpacing(2 * density, 1f)
                isClickable = true
                isFocusable = true
                val ripple = android.util.TypedValue()
                ctx.theme.resolveAttribute(android.R.attr.selectableItemBackground, ripple, true)
                setBackgroundResource(ripple.resourceId)
                setPadding(0, (6 * density).toInt(), 0, (6 * density).toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener { onClick() }
            }
            container.addView(tv)
        }

        private fun addSpacer(container: LinearLayout, height: Int) {
            val spacer = View(container.context).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
            }
            container.addView(spacer)
        }

    }

    class LoadingViewHolder(root: View) : RecyclerView.ViewHolder(root)

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_AI = 1
        private const val VIEW_TYPE_LOADING = 2

        private val DiffCallback = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(old: ChatMessage, new: ChatMessage) = old.id == new.id
            override fun areContentsTheSame(old: ChatMessage, new: ChatMessage) = old == new
        }
    }
}
