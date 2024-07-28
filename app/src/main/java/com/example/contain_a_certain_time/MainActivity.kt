package com.example.contain_a_certain_time

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalTime

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val tempMinute = 0
        val tempSecond = 0
        val resultText = findViewById<TextView>(R.id.resultText)
        val button = findViewById<Button>(R.id.checkButton)
        var resultState: ResultEnum

        button.setOnClickListener {
            val startTimeHour =
                findViewById<EditText>(R.id.startTimeInput).text.toString().toIntOrNull()
            val endTimeHour =
                findViewById<EditText>(R.id.endTimeInput).text.toString().toIntOrNull()
            val targetTimeHour =
                findViewById<EditText>(R.id.targetTimeInput).text.toString().toIntOrNull()

            // 入力値チェック
            resultState = isExpectedInput(startTimeHour, endTimeHour, targetTimeHour)
            if (resultState != ResultEnum.HOLD) {
                val writeText = makeHistoryText(
                    startTimeHour.toString(),
                    endTimeHour.toString(),
                    targetTimeHour.toString(),
                    resultState
                )
                saveHistoryFile(writeText)
                resultText.text = displayResult(resultState)
                return@setOnClickListener
            }
            //時間型に変更
            val startTime = LocalTime.of(startTimeHour!!, tempMinute, tempSecond)
            val endTime = LocalTime.of(endTimeHour!!, tempMinute, tempSecond)
            val targetTime = LocalTime.of(targetTimeHour!!, tempMinute, tempSecond)
            resultState = isContain(startTime, endTime, targetTime)
            if (resultState != ResultEnum.HOLD) {
                val writeText = makeHistoryText(
                    startTime.hour.toString(),
                    endTime.hour.toString(),
                    targetTime.hour.toString(),
                    resultState
                )
                saveHistoryFile(writeText)
                resultText.text = displayResult(resultState)
                return@setOnClickListener
            }
            // 22時~4時のような0時をまたぐ場合
            if (endTime < startTime) {
                // 0~6のように補正する
                // TODO:NULLチェックすべき
                val diffTime = getString(R.string.max_num).toInt() - startTime.hour.toLong()
                val diffStartTime = LocalTime.of(0, tempMinute, tempSecond)
                val diffEndTime = endTime.plusHours(diffTime)
                val diffTargetTime = targetTime.plusHours(diffTime)
                resultState = isContain(diffStartTime, diffEndTime, diffTargetTime)
                if (resultState != ResultEnum.HOLD) {
                    val writeText = makeHistoryText(
                        startTime.hour.toString(),
                        endTime.hour.toString(),
                        targetTime.hour.toString(),
                        resultState
                    )
                    saveHistoryFile(writeText)
                    resultText.text = displayResult(resultState)
                    return@setOnClickListener
                }
            }
            if (resultState == ResultEnum.HOLD) {
                resultState = ResultEnum.NOT_CONTAIN
                val writeText = makeHistoryText(
                    startTime.hour.toString(),
                    endTime.hour.toString(),
                    targetTime.hour.toString(),
                    resultState
                )
                saveHistoryFile(writeText)
                resultText.text = displayResult(resultState)
            }
        }
    }

    /**
     *  [menu] を配置する
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(
            R.menu.option_menu, menu
        )
        return true
    }

    /**
     *  [item] が押された時のイベント
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val message = "「" + item.title + "」が押されました。"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        if (item.itemId == R.id.history) {
            val intent = Intent(application, SubActivity::class.java)
            startActivity(intent)
        }
        return true
    }

    /**
     *  [startTime],[endTime],[targetTime]期待された入力であるかチェックする
     *  現在は0～23の整数
     */
    private fun isExpectedInput(startTime: Int?, endTime: Int?, targetTime: Int?): ResultEnum {
        // TODO:NULLチェックすべき
        val maxNum = getString(R.string.max_num).toInt()
        if (startTime == null || endTime == null || targetTime == null) {
            return ResultEnum.IS_NOT_NUMBER
        }
        if (maxNum <= startTime || maxNum <= endTime || maxNum <= targetTime) {
            return ResultEnum.OUT_OF_RANGE
        }
        return ResultEnum.HOLD
    }

    /**
     *  [targetTime] が [startTime] と [endTime]の範囲内にあるかチェックする
     */
    private fun isContain(
        startTime: LocalTime, endTime: LocalTime, targetTime: LocalTime
    ): ResultEnum {
        if (targetTime in startTime..<endTime || startTime == targetTime && targetTime == endTime) {
            return ResultEnum.CONTAIN
        }
        return ResultEnum.HOLD
    }

    /**
     *  [targetTime] [startTime] [endTime] [resultState]の形式でファイルに書き込む文字列を作る
     */
    private fun makeHistoryText(
        startTime: String, endTime: String, targetTime: String, resultState: ResultEnum
    ): String {
        val resultText = when (resultState) {
            ResultEnum.CONTAIN -> {
                getString(R.string.input_is_contain)
            }

            ResultEnum.NOT_CONTAIN -> {
                getString(R.string.input_is_not_contain)
            }

            ResultEnum.IS_NOT_NUMBER -> {
                getString(R.string.input_is_not_number)
            }

            ResultEnum.OUT_OF_RANGE -> {
                getString(R.string.input_out_of_range)
            }

            else -> {
                getString(R.string.definition_error)
            }
        }

        val historyText = buildString {
            append(startTime, " ")
            append(endTime, " ")
            append(targetTime, " ")
            append(resultText, "\r\n")
        }
        return historyText
    }

    /**
     *  [resultState] によって表示する文言を変える
     */
    private fun displayResult(resultState: ResultEnum): String {
        when (resultState) {
            ResultEnum.CONTAIN -> {
                return getString(R.string.input_is_contain)
            }

            ResultEnum.NOT_CONTAIN -> {
                return getString(R.string.input_is_not_contain)
            }

            ResultEnum.IS_NOT_NUMBER -> {
                return getString(R.string.input_is_not_number)
            }

            ResultEnum.OUT_OF_RANGE -> {
                return getString(R.string.input_out_of_range)
            }

            else -> {
                return getString(R.string.definition_error)
            }
        }
    }

    /**
     *  [writeText] をファイルに書き込む
     */
    private fun saveHistoryFile(writeText: String) {

        // ファイル書き込み用設定
        val fileName = getString(R.string.history_file_name)
        val context = applicationContext
        val file = File(context.filesDir, fileName)

        // 履歴を保存する
        try {
            FileWriter(file, true).use { writer -> writer.write(writeText) }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}