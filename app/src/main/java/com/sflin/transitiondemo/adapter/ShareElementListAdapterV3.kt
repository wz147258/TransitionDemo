package com.sflin.transitiondemo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sflin.transitiondemo.R
import com.sflin.transitiondemo.databinding.AdapterShareElementListItemV3Binding

class ShareElementListAdapterV3(private val mContext: Context, private val mList: List<Int>) :
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
        val url = mList[position]
        Glide.with(mContext)
            .load(url)
            .apply(RequestOptions().skipMemoryCache(true))
            .into(holder.binding.itemImg)

        holder.binding.itemTv.text = "position:$position,${holder.binding.root.resources.getString(R.string.item_text)}"

        holder.binding.root.setOnClickListener {
            mOnCallBack.onClick(it, url)
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(val binding: AdapterShareElementListItemV3Binding) : RecyclerView.ViewHolder(binding.root)
}
