package com.example.myapplicationac2;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private EditText etNomeTreino, etDataTreino, etDuracao;
    private Spinner spinnerTipo, spinnerIntensidade, spinnerFiltroTipo;
    private CheckBox cbConcluido, cbFiltroConcluido;
    private Button btnSalvar;
    private RecyclerView recyclerViewTreinos;
    private TextView tvTotalTempo;

    private List<Treino> treinos = new ArrayList<>();
    private TreinoAdapter adapter;
    private FirebaseFirestore db;
    private Treino currentTreino;

    private String[] tipos = {"Selecione", "Musculação", "Corrida", "Caminhada", "Ciclismo", "Funcional"};
    private String[] intensidades = {"Selecione", "Leve", "Moderada", "Intensa"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupSpinners();
        setupListeners();
        loadTreinos();
    }

    private void initializeViews() {
        etNomeTreino = findViewById(R.id.etNomeTreino);
        etDataTreino = findViewById(R.id.etDataTreino);
        etDuracao = findViewById(R.id.etDuracao);
        spinnerTipo = findViewById(R.id.spinnerTipo);
        spinnerIntensidade = findViewById(R.id.spinnerIntensidade);
        cbConcluido = findViewById(R.id.cbConcluido);
        btnSalvar = findViewById(R.id.btnSalvar);
        spinnerFiltroTipo = findViewById(R.id.spinnerFiltroTipo);
        cbFiltroConcluido = findViewById(R.id.cbFiltroConcluido);
        recyclerViewTreinos = findViewById(R.id.recyclerViewTreinos);
        tvTotalTempo = findViewById(R.id.tvTotalTempo);

        recyclerViewTreinos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TreinoAdapter(new ArrayList<>(), this::onItemClick, this::onItemLongClick);
        recyclerViewTreinos.setAdapter(adapter);
    }

    private void setupSpinners() {
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(tipoAdapter);
        spinnerFiltroTipo.setAdapter(tipoAdapter);

        ArrayAdapter<String> intensidadeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, intensidades);
        intensidadeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIntensidade.setAdapter(intensidadeAdapter);
    }

    private void setupListeners() {
        etDataTreino.setOnClickListener(v -> showDatePicker());

        btnSalvar.setOnClickListener(v -> salvarTreino());

        spinnerFiltroTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        cbFiltroConcluido.setOnCheckedChangeListener((buttonView, isChecked) -> applyFilters());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            String date = String.format("%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
            etDataTreino.setText(date);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void salvarTreino() {
        String nome = etNomeTreino.getText().toString().trim();
        String tipo = spinnerTipo.getSelectedItem().toString();
        String data = etDataTreino.getText().toString().trim();
        String duracaoStr = etDuracao.getText().toString().trim();
        String intensidade = spinnerIntensidade.getSelectedItem().toString();
        boolean concluido = cbConcluido.isChecked();

        if (nome.isEmpty()) {
            Toast.makeText(this, "Nome do treino é obrigatório", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tipo.equals("Selecione")) {
            Toast.makeText(this, "Selecione o tipo de atividade", Toast.LENGTH_SHORT).show();
            return;
        }
        if (data.isEmpty()) {
            Toast.makeText(this, "Data do treino é obrigatória", Toast.LENGTH_SHORT).show();
            return;
        }
        if (duracaoStr.isEmpty()) {
            Toast.makeText(this, "Duração é obrigatória", Toast.LENGTH_SHORT).show();
            return;
        }
        if (intensidade.equals("Selecione")) {
            Toast.makeText(this, "Selecione a intensidade", Toast.LENGTH_SHORT).show();
            return;
        }

        int duracao;
        try {
            duracao = Integer.parseInt(duracaoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Duração deve ser um número", Toast.LENGTH_SHORT).show();
            return;
        }

        Treino treino = new Treino(null, nome, concluido, intensidade, duracao, data, tipo);
        if (currentTreino != null) {
            treino.setId(currentTreino.getId());
        }

        saveToFirebase(treino);
    }

    private void saveToFirebase(Treino treino) {

        Map<String, Object> treinoData = new HashMap<>();
        treinoData.put("nomeTreino", treino.getNomeTreino());
        treinoData.put("tipoTreino", treino.getTipoTreino());
        treinoData.put("dataTreino", treino.getDataTreino());
        treinoData.put("duracaoTreino", treino.getDuracaoTreino());
        treinoData.put("intensidadeTreino", treino.getIntensidadeTreino());
        treinoData.put("foiConcluido", treino.isFoiConcluido());

        if (treino.getId() == null) {
            db.collection("treinos").add(treinoData).addOnSuccessListener(documentReference -> {
                treino.setId(documentReference.getId());
                Toast.makeText(this, "Treino salvo", Toast.LENGTH_SHORT).show();
                clearForm();
                loadTreinos();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            db.collection("treinos").document(treino.getId()).set(treinoData).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Treino atualizado", Toast.LENGTH_SHORT).show();
                clearForm();
                loadTreinos();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Erro ao atualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void clearForm() {
        etNomeTreino.setText("");
        spinnerTipo.setSelection(0);
        etDataTreino.setText("");
        etDuracao.setText("");
        spinnerIntensidade.setSelection(0);
        cbConcluido.setChecked(false);
        currentTreino = null;
        btnSalvar.setText("Salvar");
    }

    private void loadTreinos() {
        db.collection("treinos").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                treinos.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Treino treino = document.toObject(Treino.class);
                    treino.setId(document.getId());
                    treinos.add(treino);
                }
                applyFilters();
                updateTotalTempo();
            } else {
                Toast.makeText(this, "Erro ao carregar treinos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        String filtroTipo = spinnerFiltroTipo.getSelectedItem().toString();
        boolean filtroConcluido = cbFiltroConcluido.isChecked();

        List<Treino> filtered = treinos.stream()
                .filter(t -> filtroTipo.equals("Selecione") || t.getTipoTreino().equals(filtroTipo))
                .filter(t -> !filtroConcluido || t.isFoiConcluido())
                .collect(Collectors.toList());

        adapter.treinos = filtered;
        adapter.notifyDataSetChanged();
        updateTotalTempo();
    }

    private void updateTotalTempo() {
        int total = adapter.treinos.stream().mapToInt(Treino::getDuracaoTreino).sum();
        tvTotalTempo.setText("Tempo total: " + total + " minutos");
    }

    private void onItemClick(Treino treino) {
        currentTreino = treino;
        etNomeTreino.setText(treino.getNomeTreino());
        spinnerTipo.setSelection(getIndex(tipos, treino.getTipoTreino()));
        etDataTreino.setText(treino.getDataTreino());
        etDuracao.setText(String.valueOf(treino.getDuracaoTreino()));
        spinnerIntensidade.setSelection(getIndex(intensidades, treino.getIntensidadeTreino()));
        cbConcluido.setChecked(treino.isFoiConcluido());
        btnSalvar.setText("Atualizar");
    }

    private void onItemLongClick(Treino treino) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Treino")
                .setMessage("Tem certeza que deseja excluir este treino?")
                .setPositiveButton("Sim", (dialog, which) -> deleteTreino(treino))
                .setNegativeButton("Não", null)
                .show();
    }

    private void deleteTreino(Treino treino) {
        db.collection("treinos").document(treino.getId()).delete().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Treino excluído", Toast.LENGTH_SHORT).show();
            loadTreinos();
        }).addOnFailureListener(e -> Toast.makeText(this, "Erro ao excluir", Toast.LENGTH_SHORT).show());
    }

    private int getIndex(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) return i;
        }
        return 0;
    }
}
