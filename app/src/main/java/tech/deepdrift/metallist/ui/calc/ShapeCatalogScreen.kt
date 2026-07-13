package tech.deepdrift.metallist.ui.calc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import tech.deepdrift.metallist.R
import tech.deepdrift.metallist.domain.model.ProfileShape
import tech.deepdrift.metallist.ui.common.ShapeIcon

private data class ShapeItem(val shape: ProfileShape, val nameRes: Int)

private val shapeItems = listOf(
    ShapeItem(ProfileShape.Round, R.string.shape_round),
    ShapeItem(ProfileShape.PipeRound, R.string.shape_pipe_round),
    ShapeItem(ProfileShape.Square, R.string.shape_square),
    ShapeItem(ProfileShape.Hex, R.string.shape_hex),
    ShapeItem(ProfileShape.Sheet, R.string.shape_sheet),
    ShapeItem(ProfileShape.BentChannel, R.string.shape_bent_channel),
    ShapeItem(ProfileShape.PipeRect, R.string.shape_pipe_rect),
    ShapeItem(ProfileShape.Angle, R.string.shape_angle),
    ShapeItem(ProfileShape.IBeam, R.string.shape_i_beam),
    ShapeItem(ProfileShape.Channel, R.string.shape_channel),
    ShapeItem(ProfileShape.Rebar, R.string.shape_rebar),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShapeCatalogScreen(onPick: (ProfileShape) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
    ) { pad ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(pad),
        ) {
            items(shapeItems) { item ->
                ShapeGridCell(item, onPick)
            }
        }
    }
}

@Composable
private fun ShapeGridCell(item: ShapeItem, onPick: (ProfileShape) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPick(item.shape) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                ShapeIcon(shape = item.shape, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                text = stringResource(item.nameRes),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}
