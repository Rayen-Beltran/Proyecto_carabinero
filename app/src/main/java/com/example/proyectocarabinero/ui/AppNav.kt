package com.example.proyectocarabinero.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyectocarabinero.viewmodel.MainViewModel
import kotlinx.coroutines.launch

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val EDIT = "edit_profile"
    const val MATERIAS = "materias"
    const val ANUNCIOS = "anuncios"
    const val CALENDARIO = "calendario"
}

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

private val items = listOf(
    NavItem(Routes.HOME, "Inicio", Icons.Default.Home),
    NavItem(Routes.PROFILE, "Perfil", Icons.Default.Person)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNav(vm: MainViewModel) {
    val nav = rememberNavController()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route

    var materiaSeleccionada by remember { mutableStateOf<MateriaItem?>(null) }

    LaunchedEffect(route) {
        if (route != Routes.MATERIAS) {
            materiaSeleccionada = null
        }
    }

    val showChrome = route == Routes.EDIT ||
            route == Routes.MATERIAS ||
            route == Routes.ANUNCIOS ||
            route == Routes.CALENDARIO ||
            items.any { it.route == route }

    val showTopBar = showChrome && route != Routes.HOME

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var showLogout by remember { mutableStateOf(false) }

    LaunchedEffect(vm.message) {
        vm.message?.let { snackbar.showSnackbar(it); vm.consumeMessage() }
    }

    fun goTo(r: String) {
        nav.navigate(r) {
            popUpTo(Routes.HOME) { inclusive = (r == Routes.HOME) }
            launchSingleTop = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showChrome && materiaSeleccionada == null,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Text(
                    vm.user?.name ?: "Proyecto carabinero",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
                )
                items.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, null) },
                        label = { Text(item.label) },
                        selected = route == item.route,
                        onClick = { scope.launch { drawerState.close() }; goTo(item.route) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                HorizontalDivider(Modifier.padding(12.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Logout, null) },
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; showLogout = true },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            contentWindowInsets = if (route == Routes.HOME) WindowInsets(0, 0, 0, 0) else ScaffoldDefaults.contentWindowInsets,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                AnimatedVisibility(showTopBar, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    TopAppBar(
                        title = {
                            Text(
                                if (route == Routes.MATERIAS && materiaSeleccionada != null) {
                                    materiaSeleccionada!!.titulo // Muestra "Materia 2" en la cabecera verde
                                } else {
                                    when (route) {
                                        Routes.EDIT -> "Editar datos"
                                        Routes.MATERIAS -> "Materias"
                                        Routes.ANUNCIOS -> "Anuncios del Día"
                                        Routes.CALENDARIO -> "Calendario"
                                        else -> items.firstOrNull { it.route == route }?.label ?: ""
                                    }
                                }
                            )
                        },
                        navigationIcon = {
                            if (route == Routes.MATERIAS && materiaSeleccionada != null) {

                                IconButton(onClick = { materiaSeleccionada = null }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retroceder")
                                }
                            } else {

                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, "Menú")
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = { showLogout = true }) {
                                Icon(Icons.Default.Logout, "Cerrar sesión")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            },
            bottomBar = {
                AnimatedVisibility(showChrome, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    NavigationBar {
                        items.forEach { item ->
                            NavigationBarItem(
                                selected = route == item.route,
                                onClick = { goTo(item.route) },
                                icon = { Icon(item.icon, null) },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            }
        ) { pad ->
            NavHost(
                navController = nav,
                startDestination = if (vm.user != null) Routes.HOME else Routes.WELCOME,
                modifier = Modifier.padding(pad),
                enterTransition = { slideInHorizontally { it / 4 } + fadeIn() },
                exitTransition = { fadeOut() },
                popEnterTransition = { fadeIn() },
                popExitTransition = { fadeOut() }
            ) {
                composable(Routes.WELCOME) {
                    WelcomeScreen(
                        onLogin = { nav.navigate(Routes.LOGIN) },
                        onRegister = { nav.navigate(Routes.REGISTER) }
                    )
                }
                composable(Routes.LOGIN) {
                    LoginScreen(
                        vm,
                        onSuccess = {
                            nav.navigate(Routes.HOME) { popUpTo(nav.graph.id) { inclusive = true } }
                        },
                        onRegister = { nav.navigate(Routes.REGISTER) }
                    )
                }
                composable(Routes.REGISTER) {
                    RegisterScreen(
                        vm,
                        onDone = { nav.navigate(Routes.LOGIN) { popUpTo(Routes.WELCOME) } },
                        onBack = { nav.popBackStack() }
                    )
                }
                composable(Routes.HOME) {
                    HomeScreen(
                        vm = vm,
                        onMaterias = { nav.navigate(Routes.MATERIAS) },
                        onAnuncios = { nav.navigate(Routes.ANUNCIOS) },
                        onCalendario = { nav.navigate(Routes.CALENDARIO) },
                        onLogout = { showLogout = true }
                    )
                }
                composable(Routes.PROFILE) {
                    ProfileScreen(vm, onEdit = { nav.navigate(Routes.EDIT) }, onLogout = { showLogout = true })
                }
                composable(Routes.EDIT) { EditProfileScreen(vm, onDone = { nav.popBackStack() }) }

                composable(Routes.MATERIAS) {
                    MateriasScreen(
                        materiaSeleccionada = materiaSeleccionada,
                        onSeleccionarMateria = { materiaSeleccionada = it }
                    )
                }
                composable(Routes.ANUNCIOS) { AnunciosScreen() }
                composable(Routes.CALENDARIO) { CalendarioScreen() }
            }
        }
    }

    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            icon = { Icon(Icons.Default.Logout, null) },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que quieres salir?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogout = false
                    vm.logout()
                    nav.navigate(Routes.WELCOME) { popUpTo(nav.graph.id) { inclusive = true } }
                }) { Text("Salir") }
            },
            dismissButton = { TextButton(onClick = { showLogout = false }) { Text("Cancelar") } }
        )
    }
}