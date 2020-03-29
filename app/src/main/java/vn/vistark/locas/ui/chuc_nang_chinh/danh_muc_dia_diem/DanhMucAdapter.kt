package vn.vistark.locas.ui.chuc_nang_chinh.danh_muc_dia_diem

import DanhMuc
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.vistark.locas.R

class DanhMucAdapter(val danhMucs: ArrayList<DanhMuc>) : RecyclerView.Adapter<DanhMucViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DanhMucViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_danh_muc, parent, false)
        return DanhMucViewHolder(v)
    }

    override fun getItemCount(): Int {
        return danhMucs.size
    }

    override fun onBindViewHolder(holder: DanhMucViewHolder, position: Int) {
        val danhMuc = danhMucs[position]
        holder.bind(danhMuc)
    }

}