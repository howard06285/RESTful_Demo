package com.shigaga.restfuldemo.ui.main_page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.shigaga.restfuldemo.R
import com.shigaga.restfuldemo.data.Weather
import com.shigaga.restfuldemo.ui.second_page.SecondFragment
import kotlinx.android.synthetic.main.item_type_a.view.*
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter : WeatherListAdapter
    private var weathers = emptyList<Weather?>()


    companion object {
        fun newInstance() = MainFragment()

        /*private val TAG: String = MainFragment::class.java.simpleName*/
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        adapter = WeatherListAdapter()

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = adapter

        swipeRefresh.setOnRefreshListener {

            // 清除現有資料 並重新發送網路請求
            weathers = emptyList()
            adapter.notifyDataSetChanged()

            viewModel.fetchDataFromOpenData()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)



        viewModel.weathersLiveData.observe(viewLifecycleOwner, Observer {

            // 關閉無資料指示
            indicator.visibility = View.GONE
            swipeRefresh.isRefreshing = false

            weathers = it
            adapter.notifyDataSetChanged()

            viewModel.showGreetingToastIfNeed()
        })

        viewModel.toastForGreetingLiveData.observe(viewLifecycleOwner, Observer {

           if (it) {
               /* 使用 Material Design SnackBar 元件取代下方 Toast 氣泡 */
               Snackbar.make(
                   requireView(),
                   getString(R.string.welcome_back_greeting_text),
                   Snackbar.LENGTH_SHORT
               ).show()

               /*Toast.makeText(
                   context,
                   getString(R.string.welcome_back_greeting_text),
                   Toast.LENGTH_SHORT
               ).show()*/

               viewModel.setAlreadyShown()
           }
        })

        viewModel.toastWithStrInputLiveData.observe(viewLifecycleOwner, Observer {

            if (it!=null) {
                /* 使用 Material Design SnackBar 元件取代下方 Toast 氣泡 */
                Snackbar.make(
                    requireView(),
                    it,
                    Snackbar.LENGTH_SHORT
                ).show()

                /*Toast.makeText(context, it, Toast.LENGTH_SHORT).show()*/

                // 清除現有資料 並刷新
                weathers = emptyList()
                adapter.notifyDataSetChanged()

                // 顯示無資料指示 並關閉 SwipeRefresh 刷新動作
                indicator.visibility = View.VISIBLE
                swipeRefresh.isRefreshing = false
            }
        })

        viewModel.fetchDataFromOpenData()
    }


    /** 天氣資訊清單 Adapter */
    inner class WeatherListAdapter : RecyclerView.Adapter<ItemHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {

            // 分成兩種不同 row 樣式
            val view = when(viewType){
                0 -> LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_type_a, parent,false)

                else -> LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_type_b, parent,false)
            }

            return ItemHolder(view)
        }

        override fun getItemCount() = weathers.size

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {

            when(holder.itemViewType) {
                // Type A
                0 -> {holder.start?.text = weathers[position]?.startTime

                    holder.endTime?.text = weathers [position]?.endTime

                    holder.temperature?.text =
                        getString (R.string.temperature_with_unit,
                            weathers[position]?.parameter?.parameterName,
                            weathers[position]?.parameter?.parameterUnit)

                    holder.itemView.setOnClickListener {

                        goToSecondPage(holder.start?.text.toString(),
                            holder.endTime?.text.toString(),
                            holder.temperature?.text.toString()
                        )
                    }
                }
                // 其餘項目無功能，略
            }
        }

        override fun getItemViewType(position: Int) = position % 2
    }

    class ItemHolder(view: View) : RecyclerView.ViewHolder(view){
        var start : TextView? = view.startTimeTv
        var endTime : TextView? = view.endTimeTv
        var temperature : TextView? = view.temperatureTv
    }


    private fun goToSecondPage(startTime: String, endTime: String, temperature: String) {
        val fragmentTransaction = parentFragmentManager.beginTransaction()

        fragmentTransaction.setCustomAnimations(
            R.anim.enter_from_right,
            R.anim.exit_from_right,
            R.anim.enter_from_left,
            R.anim.exit_to_right)

        fragmentTransaction.replace(R.id.container,
            SecondFragment.newInstance(startTime, endTime, temperature))
                .addToBackStack(SecondFragment::class.java.simpleName)
                .commit()
    }

    override fun onDestroy() {
        weathers = emptyList()
        super.onDestroy()
    }
}
