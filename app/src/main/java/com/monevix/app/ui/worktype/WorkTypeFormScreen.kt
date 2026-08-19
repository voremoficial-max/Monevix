package com.monevix.app.ui.worktype

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.monevix.app.MonevixApplication
import com.monevix.app.util.CurrencyInputFormatter
import com.monevix.app.util.CurrencyFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkTypeFormScreen(navController: NavHostController, workTypeId: Long?) {
    val app = LocalContext.current.applicationContext as MonevixApplication
    val viewModel: WorkTypeViewModel = viewModel(
        factory = WorkTypeViewModelFactory(app.workTypeRepository)
    )
    val scope = rememberCoroutineScope()
    val isEditing = workTypeId != null

    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf(TextFieldValue("")) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(isEditing) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(workTypeId) {
        if (workTypeId != null) {
            val existing = viewModel.getWorkType(workTypeId)
            if (existing != null) {
                code = existing.code
                name = existing.name
                val text = CurrencyFormatter.format(existing.unitPrice).removePrefix("$")
                unitPrice = TextFieldValue(text = text, selection = TextRange(text.length))
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar trabajo" else "Nuevo trabajo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Código * (ej. C01)") },
                singleLine = true,
                isError = errorMessage != null,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del trabajo *") },
                singleLine = true,
                isError = errorMessage != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
            OutlinedTextField(
                value = unitPrice,
                onValueChange = { input -> unitPrice = CurrencyInputFormatter.reformat(input) },
                label = { Text("Valor por unidad *") },
                singleLine = true,
                isError = errorMessage != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Text("$", modifier = Modifier.padding(start = 12.dp)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            if (isEditing) {
                Text(
                    text = "Si cambias el valor, las liquidaciones ya guardadas con este " +
                        "trabajo conservarán el precio que tenían al momento de crearse.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(
                enabled = !isSaving,
                onClick = {
                    errorMessage = null
                    isSaving = true
                    scope.launch {
                        val result = viewModel.save(
                            existingId = workTypeId,
                            code = code,
                            name = name,
                            unitPrice = CurrencyInputFormatter.toLongOrNull(unitPrice.text)?.toString()
                                ?: unitPrice.text
                        )
                        isSaving = false
                        when (result) {
                            is SaveWorkTypeResult.Success -> navController.popBackStack()
                            is SaveWorkTypeResult.ValidationError -> errorMessage = result.message
                            is SaveWorkTypeResult.CodeAlreadyExists -> errorMessage = result.message
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text(if (isSaving) "Guardando..." else "Guardar")
            }
        }
    }
}
