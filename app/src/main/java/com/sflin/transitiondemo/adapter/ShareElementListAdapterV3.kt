package com.sflin.transitiondemo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sflin.transitiondemo.databinding.AdapterShareElementListItemV3Binding
import com.sflin.transitiondemo.model.ItemData

class ShareElementListAdapterV3 constructor(private val mContext: Context, private val mList: List<ItemData>) :
    RecyclerView.Adapter<ShareElementListAdapterV3.ViewHolder>() {

    private lateinit var mOnCallBack: OnCallBack

    interface OnCallBack {
        fun onClick(view: View, url: Int)
    }

    fun setOnClickListener(mOnCallBack: OnCallBack) {
        this.mOnCallBack = mOnCallBack
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdapterShareElementListItemV3Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemData = mList[position]
        Glide.with(mContext)
            .load(itemData.resId)
            .apply(RequestOptions().skipMemoryCache(true))
            .into(holder.binding.itemImg)

        holder.binding.itemTv.text = itemData.text

        holder.binding.root.setBackgroundColor(itemData.bgColor)
        holder.binding.root.setOnClickListener {
            mOnCallBack.onClick(it, itemData.resId)
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(val binding: AdapterShareElementListItemV3Binding) : RecyclerView.ViewHolder(binding.root)
}
