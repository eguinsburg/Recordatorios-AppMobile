package com.example.recordatorios.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.recordatorios.R

/**
 * Campo de texto controlado: no guarda estado propio, muestra [valor] y comunica cada
 * cambio con [onValorChange]. Un error se indica con isError, un ícono, un mensaje
 * textual y la propiedad semántica `error`, de modo que el color no es el único recurso.
 */
@Composable
fun CampoTextoValidado(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    maxCaracteres: Int? = null,
    singleLine: Boolean = true,
    habilitado: Boolean = true,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        isError = error != null,
        enabled = habilitado,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        trailingIcon = error?.let {
            { Icon(Icons.Filled.Warning, contentDescription = stringResource(R.string.error_icono)) }
        },
        supportingText = {
            Row(Modifier.fillMaxWidth()) {
                Text(error.orEmpty(), modifier = Modifier.weight(1f))
                if (maxCaracteres != null) Text("${valor.length}/$maxCaracteres")
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .semantics { if (error != null) error(error) },
    )
}

/**
 * Selector controlado de una opción entre [opciones] (categorías o prioridades).
 * La opción elegida se marca con un ícono además del color.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> SelectorOpciones(
    etiqueta: String,
    opciones: List<T>,
    seleccionada: T?,
    onSeleccion: (T) -> Unit,
    textoOpcion: (T) -> String,
    modifier: Modifier = Modifier,
    error: String? = null,
    ayuda: String? = null,
    habilitado: Boolean = true,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { if (error != null) error(error) },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(etiqueta, style = MaterialTheme.typography.titleSmall)
        FlowRow(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            opciones.forEach { opcion ->
                val elegida = opcion == seleccionada
                FilterChip(
                    selected = elegida,
                    onClick = { onSeleccion(opcion) },
                    enabled = habilitado,
                    label = { Text(textoOpcion(opcion)) },
                    leadingIcon = if (elegida) {
                        { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                    } else {
                        null
                    },
                )
            }
        }
        when {
            error != null -> Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = stringResource(R.string.error_icono),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            ayuda != null -> Text(
                ayuda,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
