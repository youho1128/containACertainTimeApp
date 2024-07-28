package com.example.contain_a_certain_time

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.IOException


class SubActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.enableEdgeToEdge()
        setContentView(R.layout.activity_sub)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.sub)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val returnButton = findViewById<Button>(R.id.return_button)
        // lambda式
        returnButton.setOnClickListener { finish() }



        if (isFileExists()) {
            // 履歴表示用リスト
            val historyData: ArrayList<String> = ArrayList()
            val tableText = readFile()
            val lines = tableText.split("\r\n")
            lines.forEach { line ->
                if (line.isNotEmpty()) {
                    val historyText = buildHistoryText(line.split(" "))
                    historyData.add(historyText)
                }
            }

            // リスト項目とListViewを対応付けるArrayAdapterを用意する
            val adapter: ArrayAdapter<*> =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, historyData as List<Any>)

            // ListViewにArrayAdapterを設定する
            val listView = findViewById<View>(R.id.historyList) as ListView
            listView.adapter = adapter
        }
    }

    /**
     *  historyファイルを読み込む
     */
    private fun readFile(): String {
        // ファイル書き込み用設定
        val fileName = getString(R.string.history_file_name)
        val context = applicationContext
        val file = File(context.filesDir, fileName)
        // 履歴の読み込み
        try {
            val content = File(file.toString()).readText()
            return content
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    /**
     *  historyファイルの存在確認
     */
    private fun isFileExists(): Boolean {
        val fileName = getString(R.string.history_file_name)
        val context = applicationContext
        val file = File(context.filesDir, fileName)
        return file.exists() && !file.isDirectory
    }

    /**
     *  履歴用テキストの生成
     */
    private fun buildHistoryText(elements: List<String>): String {
        return buildString {
            append(
                getString(R.string.history_start_time_label),
                " : ",
                elements[0],
                "\r\n"
            )
            append(
                getString(R.string.history_end_time_label),
                " : ",
                elements[1],
                "\r\n"
            )
            append(
                getString(R.string.history_target_time_label),
                " : ",
                elements[2],
                "\r\n"
            )
            append(getString(R.string.history_result_label), " : ", elements[3])
        }
    }
}