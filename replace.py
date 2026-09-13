import re

with open('composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/FinTrackHost.kt', 'r', encoding='utf-8') as f:
    content = f.read()

pattern = r'''        Scaffold\(
            modifier = Modifier\.fillMaxSize\(\),
            bottomBar = \{
                AnimatedVisibility\(
                    visible = showBottomBar && !showGlobalAddTransaction && !showBottomBarCustomize,
                    enter = fadeIn\(animationSpec = tween\(400\)\) \+
                            slideInVertically\(
                                animationSpec = tween\(400, easing = FastOutSlowInEasing\),
                                initialOffsetY = \{ it \}
                            \),
                    exit = fadeOut\(animationSpec = tween\(400\)\) \+
                            slideOutVertically\(
                                animationSpec = tween\(400, easing = FastOutSlowInEasing\),
                                targetOffsetY = \{ it \}
                            \)
                \) \{
                    FintrackNavigationBar\(
                        navController = navController,
                        tabs = tabs,
                        onFabClick = \{ showGlobalAddTransaction = true \},
                        onCustomize = \{ showBottomBarCustomize = true \}
                    \)
                \}
            \}
        \) \{ innerPadding ->
            Box\(
                modifier = Modifier\.fillMaxSize\(\)\.padding\(
                    top = innerPadding\.calculateTopPadding\(\),
                    bottom = innerPadding\.calculateBottomPadding\(\)
                \)
            \) \{
                // Main content fills the whole screen
                AppNavHost\(
                    navController = navController,
                    modifier = Modifier\.fillMaxSize\(\),
                    startDestination = startDestination
                \)'''

replacement = '''        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
            ) {
                // Main content fills the whole screen
                AppNavHost(
                    navController = navController,
                    modifier = Modifier.fillMaxSize(),
                    startDestination = startDestination
                )

                AnimatedVisibility(
                    visible = showBottomBar && !showGlobalAddTransaction && !showBottomBarCustomize,
                    enter = fadeIn(animationSpec = tween(400)) +
                            slideInVertically(
                                animationSpec = tween(400, easing = FastOutSlowInEasing),
                                initialOffsetY = { it }
                            ),
                    exit = fadeOut(animationSpec = tween(400)) +
                            slideOutVertically(
                                animationSpec = tween(400, easing = FastOutSlowInEasing),
                                targetOffsetY = { it }
                            ),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    FintrackNavigationBar(
                        navController = navController,
                        tabs = tabs,
                        onFabClick = { showGlobalAddTransaction = true },
                        onCustomize = { showBottomBarCustomize = true }
                    )
                }'''

new_content = re.sub(pattern, replacement, content, flags=re.MULTILINE)

with open('composeApp/src/commonMain/kotlin/com/kazemieh/composeApp/FinTrackHost.kt', 'w', encoding='utf-8') as f:
    f.write(new_content)
