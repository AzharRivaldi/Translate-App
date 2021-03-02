package com.azhar.translation.activities

import android.animation.Animator
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.azhar.translation.R
import com.azhar.translation.adapter.MainAdapter
import com.azhar.translation.model.ModelMain
import com.azhar.translation.networking.ApiEndpoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fab_menu.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    var strInputTeks: String = ""
    var strBahasaSelected: String = ""
    var strKodeBahasaSelected: String = ""

    lateinit var strBahasa: Array<String>
    lateinit var strKodeBahasa: Array<String>

    var mainAdapter: MainAdapter? = null
    var modelMain: MutableList<ModelMain> = ArrayList()
    var progressDialog: ProgressDialog? = null
    var isFABOpen = false

    companion object {
        private const val REQUEST_CODE_SPEECH_INPUT = 5000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Mohon Tunggu")
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Sedang diterjemahkan...")

        strBahasa = resources.getStringArray(R.array.languageNamesGoogle)
        strKodeBahasa = resources.getStringArray(R.array.languageCodesGoogle)

        linearTranslation.setVisibility(View.GONE)

        rvListTranslation.setLayoutManager(LinearLayoutManager(this))
        rvListTranslation.setHasFixedSize(true)

        val arrayBahasa = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, strBahasa)
        arrayBahasa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBahasa.setAdapter(arrayBahasa)
        spinnerBahasa.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<>, view: View, position: Int, id: Long) {
                strBahasaSelected = parent.getItemAtPosition(position).toString()
                strKodeBahasaSelected = strKodeBahasa[position]
                spinnerBahasa.setEnabled(true)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        })

        btnTranslation.setOnClickListener {
            strInputTeks = teksInput.getText().toString()
            if (strInputTeks.isEmpty()) {
                Toast.makeText(this@MainActivity, "Form tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            } else {
                getTerjemahan(strInputTeks, strKodeBahasaSelected)
                linearTranslation.setVisibility(View.VISIBLE)
            }
        }

        fabMore.setOnClickListener {
            if (!isFABOpen) {
                showFABMenu()
            } else {
                closeFABMenu()
            }
        }

        fabVoice.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Coba ucapkan")
            try {
                startActivityForResult(intent, Companion.REQUEST_CODE_SPEECH_INPUT)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Ups! Terjadi kesalahan", Toast.LENGTH_SHORT).show()
            }
        }

        fabCamera.setOnClickListener {
            Toast.makeText(this@MainActivity, "Masih tahap pengembangan", Toast.LENGTH_SHORT).show()
        }

        viewBcakground.setOnClickListener {
            closeFABMenu()
        }
    }

    private fun getTerjemahan(strInputTeks: String, strKodeBahasaSelected: String) {
        progressDialog.show()
        modelMain.clear()
        AndroidNetworking.get(ApiEndpoint.BASEURL)
                .addPathParameter("text", strInputTeks)
                .addPathParameter("to", strKodeBahasaSelected)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        progressDialog.dismiss()
                        try {
                            val jsonObject = response.getJSONObject("data")
                            val jsonArray = jsonObject.getJSONArray("targets")
                            for (i in 0 until jsonArray.length()) {
                                val dataModel = ModelMain()
                                val terjemahan = jsonArray[i].toString()
                                dataModel.strTranslation = terjemahan
                                modelMain.add(dataModel)
                            }
                            mainAdapter = MainAdapter(modelMain)
                            rvListTranslation.adapter = mainAdapter
                            mainAdapter.notifyDataSetChanged()
                        } catch (e: JSONException) {
                            Toast.makeText(this@MainActivity, "Oops, gagal menampilkan jenis dokumen.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onError(anError: ANError) {
                        progressDialog.dismiss()
                        Toast.makeText(this@MainActivity, "Oops! Sepertinya ada masalah dengan koneksi internet kamu.", Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun showFABMenu() {
        isFABOpen = true
        linearLayoutOne.visibility = View.VISIBLE
        linearLayoutTwo.visibility = View.VISIBLE
        viewBcakground.visibility = View.VISIBLE
        fabMore.animate().rotationBy(180f)
        linearLayoutOne.animate().translationY(-55.toFloat())
        linearLayoutTwo.animate().translationY(-100.toFloat())
    }

    private fun closeFABMenu() {
        isFABOpen = false
        viewBcakground.visibility = View.GONE
        fabMore.animate().rotation(0f)
        linearLayoutOne.animate().translationY(0f)
        linearLayoutTwo.animate().translationY(0f)
        linearLayoutTwo.animate().translationY(0f).setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {
                if (!isFABOpen) {
                    linearLayoutOne.visibility = View.GONE
                    linearLayoutTwo.visibility = View.GONE
                }
            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Companion.REQUEST_CODE_SPEECH_INPUT) {
            if (requestCode != RESULT_OK && null != data) {
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                teksInput.setText(result[0])
            }
        }
    }

    override fun onBackPressed() {
        if (isFABOpen) {
            closeFABMenu()
        } else {
            super.onBackPressed()
        }
    }

}