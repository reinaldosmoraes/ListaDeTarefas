package listadetarefas.cursoandroid.com.listadetarefas;

import android.app.Activity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.MissingFormatArgumentException;

public class MainActivity extends Activity {

    private EditText texto;
    private Button botaoAdicionar;
    private ListView listaTarefas;

    private SQLiteDatabase bancoDados;

    private ArrayAdapter<String> itensAdaptador;
    private ArrayList<String> itens;
    private ArrayList<Integer> ids; //arraylist de id's para poder apagar depois

    private AlertDialog.Builder dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {

            texto = (EditText) findViewById(R.id.textoId);
            botaoAdicionar = (Button) findViewById(R.id.botaoAdicionarId);
            listaTarefas = (ListView) findViewById(R.id.listViewId);

            //banco de dados
            bancoDados = openOrCreateDatabase("apptarefas", MODE_PRIVATE, null);

            //criar tabela
            bancoDados.execSQL("CREATE TABLE IF NOT EXISTS tarefas(id INTEGER PRIMARY KEY AUTOINCREMENT, tarefa VARCHAR)");

            botaoAdicionar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String textoDigitado = texto.getText().toString();

                    //inserir no banco
                    salvarTarefa(textoDigitado);
                }
            });

            //apagar tarefa quando clicar no elemento da lista
            listaTarefas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                    dialog = new AlertDialog.Builder(MainActivity.this);

                    dialog.setTitle("Excluir Tarefa");
                    dialog.setMessage("Deseja excluir esta tarefa?");
                    dialog.setIcon(android.R.drawable.ic_delete);

                    dialog.setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Toast.makeText(MainActivity.this, "cliquei no nao", Toast.LENGTH_SHORT).show();
                        }
                    });

                    dialog.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i2) {
                            try{
                                removerTarefa(ids.get(i));

                            }catch (Exception e){
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, "Desculpe, erro detectado", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                    dialog.create();
                    dialog.show();

                }
            });

            //metodo criado
            recuperarTarefas();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void salvarTarefa(String tarefa){
        try{
            //insere a terefa no banco recebida como parametro caso nao seja vazio
            if (tarefa.equals("")){
                Toast.makeText(MainActivity.this, "Digite uma tarefa", Toast.LENGTH_SHORT).show();
            }
            else{
                bancoDados.execSQL("INSERT INTO tarefas (tarefa) VALUES ('" + tarefa + "')");
                Toast.makeText(MainActivity.this, "Tarefa salva com sucesso", Toast.LENGTH_SHORT).show();

                //recuperar e e atualizar tarefas na lista na tela
                recuperarTarefas();
                texto.setText("");

            }

        }catch (Exception e){
            e.printStackTrace();;
        }
    }

    public void recuperarTarefas(){
        try{
            //recupera as tarefas
            Cursor cursor = bancoDados.rawQuery("SELECT * FROM tarefas ORDER BY id DESC", null);

            int indiceColunaId = cursor.getColumnIndex("id");
            int indiceColunaTarefa = cursor.getColumnIndex("tarefa");

            //criar o adaptador
            itens = new ArrayList<String>();
            ids = new ArrayList<Integer>();
            itensAdaptador = new ArrayAdapter<String>(getApplicationContext(),
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1,
                    itens);
            listaTarefas.setAdapter(itensAdaptador);

            //listar as tarefas
            cursor.moveToFirst();
            while (cursor != null){

                Log.i("RESULTADO - ", "Tarefa: " + cursor.getString(indiceColunaTarefa));
                itens.add(cursor.getString(indiceColunaTarefa));
                ids.add(Integer.parseInt(cursor.getString(indiceColunaId)));

                cursor.moveToNext();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void removerTarefa(Integer id){

        try{
            bancoDados.execSQL("DELETE FROM tarefas WHERE id =" + id);
            recuperarTarefas();
            Toast.makeText(MainActivity.this, "Tarefa removida com sucesso", Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}