package com.ultimatejw.mjcn.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ultimatejw.mjcn.R

/**
 * 공통 바텀시트 피커 컴포넌트
 * - 항목 목록을 바텀시트로 표시
 * - 선택된 항목은 보라색 배경으로 강조
 * - 항목 선택 시 콜백 호출 후 자동으로 닫힘
 */
class ListPickerBottomSheet : BottomSheetDialogFragment() {

    private var title: String = ""
    private var items: List<String> = emptyList()
    private var selectedItem: String? = null
    private var onItemSelected: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_list_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tv_picker_title).text = title

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_picker_items)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = PickerAdapter(items, selectedItem) { item ->
            onItemSelected?.invoke(item)
            dismiss() // 선택 시 바텀시트 자동 닫힘
        }
    }

    companion object {
        /**
         * 바텀시트 피커 인스턴스 생성
         * @param title 바텀시트 제목
         * @param items 선택 가능한 항목 목록
         * @param selectedItem 현재 선택된 항목 (강조 표시용)
         * @param onItemSelected 항목 선택 시 콜백
         */
        fun newInstance(
            title: String,
            items: List<String>,
            selectedItem: String? = null,
            onItemSelected: (String) -> Unit
        ): ListPickerBottomSheet {
            return ListPickerBottomSheet().apply {
                this.title = title
                this.items = items
                this.selectedItem = selectedItem
                this.onItemSelected = onItemSelected
            }
        }
    }

    /** 바텀시트 항목 어댑터 */
    private class PickerAdapter(
        private val items: List<String>,
        private val selectedItem: String?,
        private val onItemClick: (String) -> Unit
    ) : RecyclerView.Adapter<PickerAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvItem: TextView = itemView.findViewById(R.id.tv_picker_item)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_picker, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvItem.text = item

            // 선택 상태에 따라 배경색/텍스트색 변경
            if (item == selectedItem) {
                holder.tvItem.setBackgroundResource(R.drawable.bg_picker_item_selected)
                holder.tvItem.setTextColor(0xFFFFFFFF.toInt())
            } else {
                holder.tvItem.setBackgroundResource(R.drawable.bg_picker_item)
                holder.tvItem.setTextColor(0xFF111111.toInt())
            }

            holder.itemView.setOnClickListener { onItemClick(item) }
        }

        override fun getItemCount(): Int = items.size
    }
}
