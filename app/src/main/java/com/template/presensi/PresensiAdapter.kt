package com.template.presensi

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.template.presensi.model.Presensi

class PresensiAdapter(private var presensiList: List<Presensi>) :
    RecyclerView.Adapter<PresensiAdapter.PresensiViewHolder>() {

    class PresensiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val waktuTextView: TextView = itemView.findViewById(R.id.waktuTextView)
        val telatTextView: TextView = itemView.findViewById(R.id.telatTextView)
    }

    fun setData(newPresensiList: List<Presensi>) {
        presensiList = newPresensiList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresensiViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_presensi, parent, false)
        return PresensiViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PresensiViewHolder, position: Int) {
        val presensi = presensiList[position]
        holder.waktuTextView.text = presensi.waktuAbsen
        holder.telatTextView.text =
            if (presensi.telat == 1) "Terlambat (${presensi.menitTelat} menit)" else "Tepat Waktu"

        // Atur warna teks dan ketebalan sesuai kondisi (merah jika terlambat, hijau jika tepat waktu)
        val colorResId = if (presensi.telat == 1) R.color.colorRed else R.color.colorGreen
        holder.telatTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, colorResId))
        holder.telatTextView.setTypeface(null, if (presensi.telat == 1) Typeface.BOLD else Typeface.NORMAL)
    }

    override fun getItemCount(): Int {
        return presensiList.size
    }
}
