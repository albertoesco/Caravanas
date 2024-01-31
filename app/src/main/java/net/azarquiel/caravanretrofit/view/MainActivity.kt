package net.azarquiel.caravanretrofit.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import net.azarquiel.caravanretrofit.R
import net.azarquiel.caravanretrofit.databinding.ActivityMainBinding
import net.azarquiel.caravanretrofit.model.Comunidad
import net.azarquiel.caravanretrofit.model.Usuario
import net.azarquiel.caravanretrofit.viewmodel.MainViewModel
import net.azarquiel.recyclerviewpajaros.adapter.AdapterComunidad

class MainActivity : AppCompatActivity() {

    private lateinit var titulo: String
    private lateinit var sh: SharedPreferences
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: AdapterComunidad
    private lateinit var binding: ActivityMainBinding
    private var usuario:Usuario?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        titulo = title.toString()
        sh = getSharedPreferences("usuario",Context.MODE_PRIVATE)
        getUsuarioSH()
        setSupportActionBar(binding.toolbar)
        initRV()

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.getComunidades().observe(this, Observer {
            it?.let{
                adapter.setComunidades(it)
            }
        })

    }

    private fun getUsuarioSH() {
        val usuariotxt = sh.getString("usuario", null)
        usuariotxt?.let {
            usuario = Gson().fromJson(usuariotxt, Usuario::class.java)
            title = "${titulo} - ${usuario!!.nick}"
        }
    }

    private fun initRV() {
        adapter = AdapterComunidad(this, R.layout.rowcomunidad)
        binding.cm.rvcomunidad.adapter = adapter
        binding.cm.rvcomunidad.layoutManager = LinearLayoutManager(this)
    }

    fun onClickComunidad(v: View) {
        val comunidad = v.tag as Comunidad
        val intent = Intent(this, ProvinciasActivity::class.java)
        intent.putExtra("comunidad", comunidad)
        intent.putExtra("usuario", usuario)
        startActivity(intent)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_user -> {
                // programar.....
                onClickLoginRegister()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onClickLoginRegister() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Login/Register")
        val ll = LinearLayout(this)
        ll.setPadding(30,30,30,30)
        ll.orientation = LinearLayout.VERTICAL

        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        lp.setMargins(0,50,0,50)

        val textInputLayoutNick = TextInputLayout(this)
        textInputLayoutNick.layoutParams = lp
        val etnick = EditText(this)
        etnick.setPadding(0, 80, 0, 80)
        etnick.textSize = 20.0F
        etnick.hint = "Nick"
        textInputLayoutNick.addView(etnick)

        val textInputLayoutPass = TextInputLayout(this)
        textInputLayoutPass.layoutParams = lp
        val etpass = EditText(this)
        etpass.setPadding(0, 80, 0, 80)
        etpass.textSize = 20.0F
        etpass.hint = "Pass"
        textInputLayoutPass.addView(etpass)
        ll.addView(textInputLayoutNick)
        ll.addView(textInputLayoutPass)
        builder.setView(ll)

        builder.setPositiveButton("Aceptar") { dialog, which ->
            if (etnick.text.isEmpty() || etpass.text.isEmpty())
                Toast.makeText(this, "Rellena los dos campos...", Toast.LENGTH_SHORT).show()
            else
                login(etnick.text.toString(), etpass.text.toString())
        }

        builder.setNegativeButton("Cancelar") { dialog, which ->
        }

        builder.show()
    }

    private fun login(nick: String, pass: String) {
        viewModel.getUsuario(nick,pass).observe(this, Observer {
            if (it==null){
                viewModel.saveUsuario(Usuario(0,nick,pass)).observe(this, Observer {
                    it?.let {
                        usuario = it
                        saveSH()
                        Toast.makeText(this, "No login, Register ok...", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            else {
                Toast.makeText(this, "Login ok...", Toast.LENGTH_SHORT).show()
                usuario = it
                saveSH()
            }
        })
    }

    private fun saveSH() {
        val editor = sh.edit()
        editor.putString("usuario", Gson().toJson(usuario))
        editor.commit()
        title = "${titulo} - ${usuario!!.nick}"
    }


}