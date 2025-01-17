import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.bottomnavigation.R
import com.example.bottomnavigation.data.FavoriteAddress
import com.example.bottomnavigation.databinding.FragmentItemFavoriteAddressBinding

class FavoriteAddressAdapter(private var items: MutableList<FavoriteAddress>)
    : RecyclerView.Adapter<FavoriteAddressAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FragmentItemFavoriteAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<FavoriteAddress>) {
        items.clear() // 기존 항목을 모두 제거
        items.addAll(newItems) // 새로운 리스트 항목을 추가
        notifyDataSetChanged() // 데이터가 변경되었음을 어댑터에 알림
    }

    class ViewHolder(private val binding: FragmentItemFavoriteAddressBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FavoriteAddress) {
            binding.addressTitle.text = item.title
            binding.addressDescr.text = item.descr
            binding.itemCheckBox.isChecked = item.isChecked

            binding.itemCheckBox.setOnCheckedChangeListener { _, isChecked ->
                item.isChecked = isChecked
            }
        }
    }
}

