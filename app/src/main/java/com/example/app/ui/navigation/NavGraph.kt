package com.example.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.app.data.model.Testament
import com.example.app.ui.booklist.BookListScreen
import com.example.app.ui.booklist.BookListViewModel
import com.example.app.ui.chapterlist.ChapterListScreen
import com.example.app.ui.chapterlist.ChapterListViewModel
import com.example.app.ui.search.SearchScreen
import com.example.app.ui.search.SearchViewModel
import com.example.app.ui.testament.TestamentSelectionScreen
import com.example.app.ui.verse.VerseScreen
import com.example.app.ui.verse.VerseViewModel

@Composable
fun BibleNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.TestamentSelection.route) {

        composable(Screen.TestamentSelection.route) {
            TestamentSelectionScreen(
                onTestamentClick = { navController.navigate(Screen.BookList.createRoute(it)) },
                onSearchClick = { navController.navigate(Screen.Search.route) }
            )
        }

        composable(Screen.Search.route) {
            val vm: SearchViewModel = viewModel(factory = SearchViewModel.Factory)
            SearchScreen(
                viewModel = vm,
                onNavigateToVerse = { bookId, chapter, verse ->
                    navController.navigate(Screen.Verse.createRoute(bookId, chapter, verse))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.BookList.route,
            arguments = listOf(navArgument("testament") { type = NavType.StringType })
        ) { backStackEntry ->
            val testament = Testament.valueOf(
                backStackEntry.arguments?.getString("testament") ?: Testament.OLD.name
            )
            val vm: BookListViewModel = viewModel(factory = BookListViewModel.Factory)
            BookListScreen(
                viewModel = vm,
                testament = testament,
                onBookClick = { navController.navigate(Screen.ChapterList.createRoute(it)) },
                onBack = { navController.popBackStack() },
                onSearchClick = { navController.navigate(Screen.Search.route) }
            )
        }

        composable(
            route = Screen.ChapterList.route,
            arguments = listOf(navArgument("bookId") { type = NavType.IntType })
        ) {
            val vm: ChapterListViewModel = viewModel(factory = ChapterListViewModel.Factory)
            ChapterListScreen(
                viewModel = vm,
                onChapterClick = { bookId, chapterNumber ->
                    navController.navigate(Screen.Verse.createRoute(bookId, chapterNumber))
                },
                onBack = { navController.popBackStack() },
                onSearchClick = { navController.navigate(Screen.Search.route) }
            )
        }

        composable(
            route = Screen.Verse.route,
            arguments = listOf(
                navArgument("bookId") { type = NavType.IntType },
                navArgument("chapterNumber") { type = NavType.IntType },
                navArgument("verseAnchor") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            val verseAnchor = backStackEntry.arguments?.getInt("verseAnchor")?.takeIf { it > 0 }
            val vm: VerseViewModel = viewModel(factory = VerseViewModel.Factory)
            VerseScreen(
                viewModel = vm,
                verseAnchor = verseAnchor,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
