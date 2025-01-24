import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bottomnavigation.data.FavoriteAddress
import com.example.bottomnavigation.databinding.FragmentItemFavoriteAddressBinding

class FavoriteAddressAdapter(private val addresses: MutableList<FavoriteAddress>,
                             private val onItemCheckedChanged: () -> Unit  // 아이템 상태 변경 시 호출할 함수
) : RecyclerView.Adapter<FavoriteAddressAdapter.AddressViewHolder>() {

    inner class AddressViewHolder(val binding: FragmentItemFavoriteAddressBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(address: FavoriteAddress) {
            binding.itemCheckBox.isChecked = address.isChecked

            // 체크박스 리스너 설정
            binding.itemCheckBox.setOnCheckedChangeListener { _, isChecked ->
                address.isChecked = isChecked
                onItemCheckedChanged() // 각 아이템 상태 변경될 때마다 호출 됨
            }

            binding.addressTitle.text = address.title
            binding.addressDescr.text = address.descr
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val binding = FragmentItemFavoriteAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        holder.bind(addresses[position])
    }

    override fun getItemCount() = addresses.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: MutableList<FavoriteAddress>) {
        addresses.clear() // 기존 리스트 제거
        addresses.addAll(newList) // 새 리스트 추가
        notifyDataSetChanged() // 데이터 갱신 알림
    }
}
