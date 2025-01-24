package com.example.bottomnavigation.ui.bookmark


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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bottomnavigation.R
import com.example.bottomnavigation.data.FavoriteAddress
import com.example.bottomnavigation.databinding.FragmentBookmarkBinding
import com.example.bottomnavigation.models.SharedWeatherViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BookmarkFragment : Fragment() {
    private val sharedWeatherViewModel: SharedWeatherViewModel by activityViewModels()
    private lateinit var sharedPreferences: SharedPreferences
    private var _binding: FragmentBookmarkBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: FavoriteAddressAdapter
    private var addrList: MutableList<FavoriteAddress> = mutableListOf()
    private lateinit var bookmarkButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LC_LF", "onCreate: LocationFragment")

        sharedPreferences = requireActivity().getSharedPreferences("sp", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        Log.d("LC_LF", "onCreateView: LocationFragment")
        _binding = FragmentBookmarkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("LC_LF", "onViewCreated: LocationFragment")

        setupToolbar(binding.toolbarLocation)
        setupViews()
        observeViewModel()
        loadItems()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        Log.d("LC_LF", "onResume: LocationFragment")
    }



    @SuppressLint("NotifyDataSetChanged")
    private fun setupViews() {
        binding.deleteBtn.setOnClickListener {
            bookmarkButton = requireActivity().findViewById(R.id.fragmentHome_imageButtonBookmark)
            val isSelectAllChecked = binding.selectAllCheckBox.isChecked

            if (isSelectAllChecked) {
                addrList.clear()
                sharedWeatherViewModel.updateAddresses(mutableListOf())
                sharedPreferences.edit().remove("address_list").apply()
                Log.d("DeleteAction", "All bookmarked addresses deleted.")
            } else {
                val bookmarkedAddresses = sharedWeatherViewModel.addresses.value?.filter { it.isBookmarked } ?: emptyList()
                val toDelete = addrList.filter { it.isChecked }
                addrList.removeAll(toDelete)
                val updatedBookmarkedAddresses = bookmarkedAddresses.filterNot { toDelete.contains(it) }.toMutableList()
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
            addrList.forEach { it.isChecked = isChecked }
            adapter.notifyDataSetChanged()
        }


        val curLoc = requireActivity().findViewById<TextView>(R.id.fragmentHomeTop_textViewCurrLocation).text.toString()
        binding.LocationFragmentCurLoc.text = curLoc
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
        adapter = FavoriteAddressAdapter(addrList){
            checkIfAllItemsAreChecked()
        }
        sharedWeatherViewModel.addresses.observe(viewLifecycleOwner, Observer { addresses->
            binding.recyclerViewBookMarked.adapter = adapter
            binding.recyclerViewBookMarked.layoutManager = LinearLayoutManager(requireContext())
            Log.d("LocationFragment", "Observed addresses: $addresses") // 로드된 데이터 확인
            adapter.submitList(addresses.toMutableList()) // RecyclerView에 데이터 업데이트
        })

    }


    fun checkIfAllItemsAreChecked() {
        val isAllChecked = addrList.all { it.isChecked }
        binding.selectAllCheckBox.isChecked = isAllChecked
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
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar) // 액티비티 툴바를 액션바로 설정
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true) // 액션바 뒤로가기 버튼 활성화
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()

        }
    }


    // ● 북마크아이콘
    private fun updateBookmarkIcon(isBookmarked: Boolean) {
        requireActivity().runOnUiThread {
            if (isBookmarked) {
                bookmarkButton.setImageResource(R.drawable.star_yellow)
                bookmarkButton.tag = R.drawable.star_yellow
            } else {
                bookmarkButton.setImageResource(R.drawable.star)
                bookmarkButton.tag = R.drawable.star
            }
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
