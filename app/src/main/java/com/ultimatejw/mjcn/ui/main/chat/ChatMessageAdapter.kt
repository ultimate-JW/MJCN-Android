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

class ChatMessageAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DiffCallback) {

    var onSuggestionClick: ((String) -> Unit)? = null

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).isFromUser) VIEW_TYPE_USER else VIEW_TYPE_AI

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_USER) {
            UserViewHolder(inflater.inflate(R.layout.item_chat_message_user, parent, false))
        } else {
            AiViewHolder(inflater.inflate(R.layout.item_chat_message_ai, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is UserViewHolder -> holder.bind(message.content)
            is AiViewHolder   -> holder.bind(message.content, onSuggestionClick)
        }
    }

    class UserViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        private val tvMessage: TextView = root.findViewById(R.id.tv_message)
        fun bind(content: String) { tvMessage.text = content }
    }

    class AiViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        private val tvHeader: TextView = root.findViewById(R.id.tv_header)
        private val containerContent: LinearLayout = root.findViewById(R.id.container_content)
        private val containerSuggestions: LinearLayout = root.findViewById(R.id.container_suggestions)
        private val containerSuggestionItems: LinearLayout = root.findViewById(R.id.container_suggestion_items)

        fun bind(content: String, onSuggestionClick: ((String) -> Unit)?) {
            val ctx = itemView.context
            val density = ctx.resources.displayMetrics.density

            containerContent.removeAllViews()
            containerSuggestionItems.removeAllViews()

            val parsed = parseAiContent(content)

            // 보라색 헤더 칩
            if (parsed.header.isNotBlank()) {
                tvHeader.text = parsed.header
                tvHeader.visibility = View.VISIBLE
            } else {
                tvHeader.visibility = View.GONE
            }

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
                        else -> addBodyText(ctx, containerContent, line.trimStart(), density)
                    }
                }
            }

            // URL 링크 버튼
            if (parsed.links.isNotEmpty()) {
                addSpacer(containerContent, (8 * density).toInt())
                parsed.links.forEach { (label, url) ->
                    addLinkButton(ctx, containerContent, label ?: url, url, density)
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
            val header: String,
            val mainBlocks: List<String>,
            val suggestions: List<String>,
            val links: List<Pair<String?, String>>
        )

        private fun parseAiContent(raw: String): ParsedContent {
            val links = extractLinks(raw)
            var cleaned = raw
            Regex("\\[([^\\]]+)\\]\\((https?://[^)\\s]+)\\)").findAll(raw).forEach {
                cleaned = cleaned.replace(it.value, "")
            }
            links.filter { it.first == null }.forEach { (_, url) ->
                cleaned = cleaned.replace(url, "")
            }

            val blocks = cleaned.split(Regex("\\n{2,}"))
                .map { it.trim() }
                .filter { it.isNotBlank() }

            if (blocks.isEmpty()) return ParsedContent("", emptyList(), emptyList(), links)

            // 첫 블록이 한 줄이고 짧으면 헤더 칩으로 사용
            val firstBlock = blocks.first()
            val isShortSingleLine = firstBlock.lines().size == 1 && firstBlock.length <= 80
                    && !firstBlock.trimStart().startsWith("#")
                    && !firstBlock.trimStart().matches(Regex("^\\d+\\.\\s+.+"))
            val header = if (isShortSingleLine) firstBlock else ""
            val remaining = if (isShortSingleLine) blocks.drop(1) else blocks

            // "추가로 물을 수 있어요" 섹션 파싱
            val suggestIdx = remaining.indexOfFirst { block ->
                block.trimStart().startsWith("추가로 물을 수 있어요")
            }

            val mainBlocks: List<String>
            val suggestions: List<String>

            if (suggestIdx >= 0) {
                mainBlocks = remaining.subList(0, suggestIdx)
                val suggestBlock = remaining[suggestIdx]
                suggestions = suggestBlock.lines()
                    .drop(1)
                    .filter { it.trimStart().startsWith("-") || it.trimStart().startsWith("•") || it.trimStart().startsWith("\"") || it.trimStart().startsWith("“") }
                    .map { line ->
                        line.trimStart('-', '•', '"', '“', ' ')
                            .trimEnd('"', '”')
                            .trim()
                    }
                    .filter { it.isNotBlank() }
            } else {
                mainBlocks = remaining
                suggestions = emptyList()
            }

            return ParsedContent(header, mainBlocks, suggestions, links)
        }

        private fun renderMarkdown(text: String): SpannableStringBuilder {
            val sb = SpannableStringBuilder()
            val boldRegex = Regex("\\*\\*(.+?)\\*\\*")
            var lastEnd = 0
            boldRegex.findAll(text).forEach { match ->
                if (match.range.first > lastEnd) {
                    sb.append(text.substring(lastEnd, match.range.first))
                }
                val start = sb.length
                sb.append(match.groupValues[1])
                sb.setSpan(StyleSpan(Typeface.BOLD), start, sb.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                lastEnd = match.range.last + 1
            }
            if (lastEnd < text.length) sb.append(text.substring(lastEnd))
            return sb
        }

        private fun addSectionTitle(ctx: android.content.Context, container: LinearLayout, text: String, density: Float) {
            val tv = TextView(ctx).apply {
                this.text = renderMarkdown(text)
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
                this.text = renderMarkdown("• $text")
                textSize = 14f
                typeface = ResourcesCompat.getFont(ctx, R.font.pretendard_regular)
                setTextColor(ctx.getColor(R.color.font_color1_1))
                setLineSpacing(2 * density, 1f)
                setPadding((4 * density).toInt(), 0, 0, 0)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = (4 * density).toInt() }
            }
            container.addView(tv)
        }

        private fun addBodyText(ctx: android.content.Context, container: LinearLayout, text: String, density: Float) {
            val tv = TextView(ctx).apply {
                this.text = renderMarkdown(text)
                textSize = 14f
                typeface = ResourcesCompat.getFont(ctx, R.font.pretendard_regular)
                setTextColor(ctx.getColor(R.color.font_color2))
                setLineSpacing(2 * density, 1f)
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
                this.text = "“$text”"
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

        private fun extractLinks(text: String): List<Pair<String?, String>> {
            val result = mutableListOf<Pair<String?, String>>()
            val mdRegex = Regex("\\[([^\\]]+)\\]\\((https?://[^)\\s]+)\\)")
            mdRegex.findAll(text).forEach {
                result.add(it.groupValues[1] to it.groupValues[2])
            }
            val capturedUrls = result.map { it.second }.toSet()
            Regex("https?://[^\\s]+").findAll(text).forEach {
                if (it.value !in capturedUrls) result.add(null to it.value)
            }
            return result
        }
    }

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_AI = 1

        private val DiffCallback = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(old: ChatMessage, new: ChatMessage) = old.id == new.id
            override fun areContentsTheSame(old: ChatMessage, new: ChatMessage) = old == new
        }
    }
}
