package tech.deepdrift.metallist.ui.nav

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.RuleFolder
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import tech.deepdrift.metallist.R
import tech.deepdrift.metallist.domain.model.ProfileShape
import tech.deepdrift.metallist.ui.application.ApplicationScreen
import tech.deepdrift.metallist.ui.calc.CalcScreen
import tech.deepdrift.metallist.ui.calc.ShapeCatalogScreen
import tech.deepdrift.metallist.ui.history.HistoryScreen
import tech.deepdrift.metallist.ui.iso.IsoScreen
import tech.deepdrift.metallist.ui.materials.MaterialsScreen

sealed class Screen(val route: String) {
    data object Catalog : Screen("catalog")
    data class Calc(val shape: ProfileShape, val historyId: Long = 0L) :
        Screen("calc/${shape.name}?historyId=$historyId") {
        companion object {
            const val ROUTE_PATTERN = "calc/{shape}?historyId={historyId}"
        }
    }
    data object Iso : Screen("iso")
    data object Materials : Screen("materials")
    data object History : Screen("history")
}

private data class TabItem(
    val route: String,
    val label: Int,
    val icon: ImageVector,
)

private val tabs = listOf(
    TabItem(Screen.Catalog.route, R.string.section_calculator, Icons.Outlined.Category),
    TabItem(Screen.Iso.route, R.string.section_iso, Icons.Outlined.RuleFolder),
    TabItem(Screen.Materials.route, R.string.section_materials, Icons.Outlined.Science),
    TabItem(Screen.History.route, R.string.section_history, Icons.Outlined.History),
)

@Composable
fun MetallistNavHost() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route ?: Screen.Catalog.route
    val onTabRoot = tabs.any { it.route == currentRoute }

    Scaffold(
        bottomBar = { if (onTabRoot) BottomBar(nav, currentRoute) },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Screen.Catalog.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Screen.Catalog.route) {
                ShapeCatalogScreen(onPick = { shape ->
                    nav.navigate("calc/${shape.name}?historyId=0")
                })
            }
            composable(Screen.Calc.ROUTE_PATTERN) { backStack ->
                val shapeName = backStack.arguments?.getString("shape") ?: ProfileShape.Round.name
                val historyId = backStack.arguments?.getString("historyId")?.toLongOrNull() ?: 0L
                CalcScreen(
                    shape = ProfileShape.valueOf(shapeName),
                    historyId = historyId,
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Screen.Iso.route) { IsoScreen() }
            composable(Screen.Materials.route) { MaterialsScreen() }
            composable(Screen.History.route) {
                HistoryScreen(
                    onOpen = { historyId, shapeName, kind ->
                        if (kind == "metal") {
                            nav.navigate("calc/$shapeName?historyId=$historyId")
                        } else {
                            nav.navigate(Screen.Iso.route)
                        }
                    },
                    onCreateApplication = { ids ->
                        val idsArg = ids.joinToString(",")
                        nav.navigate("application?ids=$idsArg")
                    },
                )
            }
            composable("application?ids={ids}") {
                ApplicationScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}

@Composable
private fun BottomBar(nav: NavHostController, currentRoute: String) {
    NavigationBar {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    if (currentRoute != tab.route) {
                        nav.navigate(tab.route) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(stringResource(tab.label)) },
            )
        }
    }
}
