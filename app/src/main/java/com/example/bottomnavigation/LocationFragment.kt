package com.example.bottomnavigation

import FavoriteAddressAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bottomnavigation.data.FavoriteAddress
import com.example.bottomnavigation.databinding.FragmentLocationBinding
import com.example.bottomnavigation.models.SharedWeatherViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocationFragment : Fragment() {
    private val sharedWeatherViewModel: SharedWeatherViewModel by activityViewModels()
    private lateinit var sharedPreferences: SharedPreferences
    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: FavoriteAddressAdapter
    private var addrList: MutableList<FavoriteAddress> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = requireActivity().getSharedPreferences("sp", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(binding.toolbarLocation)
        setupViews()
        observeViewModel()
        loadItems()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()

        val currentLocation = sharedWeatherViewModel.currentLocation.value ?: return

        // 현재 위치가 북마크에 저장되어 있는지 확인
        val isBookmarked = sharedWeatherViewModel.isAddressBookmarked(currentLocation)

        // 북마크 버튼 상태 업데이트
        val bookmarkBtn = requireActivity().findViewById<ImageButton>(R.id.fragmentHome_imageButtonBookmark)
        if(isBookmarked){
            bookmarkBtn.setImageResource(R.drawable.star_yellow)
        }else{
            bookmarkBtn.setImageResource(R.drawable.star)
        }
    }



    @SuppressLint("NotifyDataSetChanged")
    private fun setupViews() {
        binding.deleteBtn.setOnClickListener {
            val isSelectAllChecked = binding.selectAllCheckBox.isChecked

            if (isSelectAllChecked) {
                addrList.clear()
                sharedWeatherViewModel.updateAddresses(emptyList())
                sharedPreferences.edit().remove("address_list").apply()
                Log.d("DeleteAction", "All bookmarked addresses deleted.")
            } else {
                val bookmarkedAddresses = sharedWeatherViewModel.addresses.value?.filter { it.isBookmarked } ?: emptyList()
                val toDelete = addrList.filter { it.isChecked }
                addrList.removeAll(toDelete)
                val updatedBookmarkedAddresses = bookmarkedAddresses.filterNot { toDelete.contains(it) }
                sharedWeatherViewModel.updateAddresses(updatedBookmarkedAddresses)
                sharedWeatherViewModel.saveAddresses(requireContext())

                Log.d("DeleteAction", "Selected bookmarked addresses deleted.")
                toDelete.forEach { deletedAddress ->
                    Log.d("DeleteAction", "Deleted: ${deletedAddress.title}")
                }
            }
            adapter.notifyDataSetChanged()
        }


        binding.selectAllCheckBox.setOnClickListener {
            val isChecked = binding.selectAllCheckBox.isChecked
            Toast.makeText(requireActivity(), "checked: $isChecked", Toast.LENGTH_LONG).show()
            addrList.forEach { it.isChecked = isChecked }
            adapter.notifyDataSetChanged()
            updateSelectAllCheckBoxState()
        }

        val curLoc = requireActivity().findViewById<TextView>(R.id.fragmentHomeTop_textViewCurrLocation).text.toString()
        binding.LocationFragmentCurLoc.text = curLoc
        saveStringInPreferences("updatedAddress", curLoc)
    }


    private fun removeCheckedItemsAndUpdatePreferences() {
        // 체크된 아이템들을 리스트에서 제거
        val checkedItems = addrList.filter { it.isChecked }
        addrList.removeAll(checkedItems)

        // SharedPreferences에서 전체 주소 목록을 가져온다.
        val json = sharedPreferences.getString("address_list", null)
        val type = object : TypeToken<MutableList<FavoriteAddress>>() {}.type
        var storedAddresses: MutableList<FavoriteAddress> = if (json != null) {
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }

        // 체크된 아이템들과 동일한 주소를 가진 항목들을 SharedPreferences에서 제거
        storedAddresses = storedAddresses.filterNot { item ->
            checkedItems.any { checkedItem ->
                item.title == checkedItem.title
            }
        }.toMutableList()

        // 변경된 주소 목록을 다시 JSON 형식으로 변환하여 저장
        val editor = sharedPreferences.edit()
        val updatedJson = Gson().toJson(storedAddresses)
        editor.putString("address_list", updatedJson)
        editor.apply()
    }

    // ■ SharedPreference 문자열 저장하는 함수
    private fun saveStringInPreferences(key: String, string: String) {
        val editor : SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(key, string)
        editor.apply()
    }


    private fun observeViewModel() {
        sharedWeatherViewModel.addresses.observe(viewLifecycleOwner, Observer { addresses ->
            addrList.clear()
            addrList.addAll(addresses)
            adapter.notifyDataSetChanged()
            updateSelectAllCheckBoxState()
        })
    }





    private fun updateSelectAllCheckBoxState() {
        // 아이템 1개 이상에, 전부 체크돼있으면
        val allChecked = addrList.isNotEmpty() && addrList.all { it.isChecked }
        binding.selectAllCheckBox.isChecked = allChecked

            // 아이템 없으면 "전체선택" 체크 해제
        if (addrList.isEmpty()) {
            binding.selectAllCheckBox.isChecked = false
        }
    }


    private fun setupRecyclerView() {
        adapter = FavoriteAddressAdapter(addrList)
        binding.recyclerViewBookMarked.adapter = adapter
        binding.recyclerViewBookMarked.layoutManager = LinearLayoutManager(requireContext())
    }



    private fun loadItems() {
        val json = sharedPreferences.getString("address_list", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<FavoriteAddress>>() {}.type
            addrList = Gson().fromJson(json, type)
        }
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.title = "관심지역 설정"
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun logSharedPreferences() {
        val allEntries = sharedPreferences.all
        for ((key, value) in allEntries) {
            Log.d("SharedPrefs", "$key: $value")
        }
    }

}
