package com.example.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.app.ui.booklist.BookListScreen
import com.example.app.ui.booklist.BookListViewModel
import com.example.app.ui.chapterlist.ChapterListScreen
import com.example.app.ui.chapterlist.ChapterListViewModel
import com.example.app.ui.verse.VerseScreen
import com.example.app.ui.verse.VerseViewModel

@Composable
fun BibleNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.BookList.route) {

        composable(Screen.BookList.route) {
            val vm: BookListViewModel = viewModel(factory = BookListViewModel.Factory)
            BookListScreen(
                viewModel = vm,
                onBookClick = { bookId ->
                    navController.navigate(Screen.ChapterList.createRoute(bookId))
                }
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
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Verse.route,
            arguments = listOf(
                navArgument("bookId") { type = NavType.IntType },
                navArgument("chapterNumber") { type = NavType.IntType }
            )
        ) {
            val vm: VerseViewModel = viewModel(factory = VerseViewModel.Factory)
            VerseScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
