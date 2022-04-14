package com.agrimetal.yorimichi2.Adaptor

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.agrimetal.yorimichi2.R

class Footer(var itemText: String)

class Header(var itemText: String)

class SettingSourceHolder(){
    public val contentToSource = mutableMapOf<String, Any>()
    public val contentToLink = mutableMapOf<String, String>()

    init {
        contentToSource["アプリを評価する"] = R.drawable.star
        contentToSource["アプリを共有する"] = R.drawable.share
        contentToSource["サービス利用規約"] = R.drawable.document
        contentToSource["プライバシーポリシー"] = R.drawable.hand
        contentToSource["ヘルプはこちら"] = R.drawable.help

        contentToLink["アプリを評価する"] = "https://apps.apple.com/jp/app/yorimichiapp/id1596625712"
        contentToLink["アプリを共有する"] = "https://yorimichi-project.webflow.io/"
        contentToLink["サービス利用規約"] = "https://yorimichi-project.webflow.io/image-license-info"
        contentToLink["プライバシーポリシー"] = "https://yorimichi-privacy-policy.webflow.io/"
        contentToLink["ヘルプはこちら"] = "https://yorimichi-project.webflow.io/"
    }
}

class SettingItemAdapter(private val mContext: Context, private val mData: ArrayList<Any>) : BaseAdapter() {
    private val NORMAL_ITEM = 0
    private val HEADER_ITEM = 1
    private val FOOTER_ITEM = 2


    private val mInflater: LayoutInflater
    override fun getCount(): Int {
        return mData.size
    }

    override fun getItem(position: Int): Any {
        return mData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getViewTypeCount(): Int {
        return 3
    }

    override fun getItemViewType(position: Int): Int {
        val mObject = getItem(position)
        if (mObject is String) {
            return NORMAL_ITEM
        } else if (mObject is Header) {
            return HEADER_ITEM
        } else if (mObject is Footer) {
            return FOOTER_ITEM
        } else {
            return IGNORE_ITEM_VIEW_TYPE
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val rowType = getItemViewType(position)
        val obj = getItem(position)
        val view: View
        val textView: TextView
        val imageView: ImageView
        val settingSourceHolder = SettingSourceHolder()
        when (rowType) {
            NORMAL_ITEM -> {
                view = mInflater.inflate(R.layout.profile_settings_list_item, parent, false)
                textView = view.findViewById(R.id.item_profile_settings)
                imageView = view.findViewById(R.id.item_image_profile_settings)
                val content = obj as String
                textView.text = content
                imageView.setImageResource(settingSourceHolder.contentToSource[content] as Int)

                view.setOnClickListener{
                    val uri = Uri.parse(settingSourceHolder.contentToLink[content]);
                    val i = Intent(Intent.ACTION_VIEW,uri);
                    mContext.startActivity(i);
                }

            }
            HEADER_ITEM -> {

                view = mInflater.inflate(R.layout.profile_settings_list_section, parent, false)
                textView = view.findViewById(R.id.section_profile_settings)
                // Set the background to red because it is a Header row
                //textView.setBackgroundColor(Color.RED)
                // Cast the object from generic list to Header type
                val header = obj as Header
                textView.text = header.itemText
            }
            FOOTER_ITEM -> {
                view = mInflater.inflate(R.layout.profile_settings_list_item, parent, false)
                textView = view.findViewById(R.id.item_profile_settings)
                // Set the background to green because it is a Header row
                textView.setBackgroundColor(Color.GREEN)
                val footer = obj as Footer
                textView.text = footer.itemText
            }
            else -> return convertView
        }
        return view
    }


    init {
        mInflater = LayoutInflater.from(mContext)
    }
}
