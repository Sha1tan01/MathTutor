package ru.mathtutor.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import ru.mathtutor.app.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val topLevelDestinations = setOf(
        R.id.homeFragment,
        R.id.sectionsFragment,
        R.id.chatFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupBottomNavigation()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevel = destination.id in topLevelDestinations
            binding.bottomNavigation.visibility =
                if (isTopLevel) View.VISIBLE else View.GONE

            binding.navHostFragment.post {
                val view = binding.navHostFragment
                val params = view.layoutParams
                if (params is androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams) {
                    val newMargin = if (isTopLevel) {
                        resources.getDimensionPixelSize(R.dimen.bottom_nav_height)
                    } else 0
                    if (params.bottomMargin != newMargin) {
                        params.bottomMargin = newMargin
                        view.layoutParams = params
                    }
                }
            }

            // Sync bottom nav selection
            val menuItemId = when (destination.id) {
                R.id.homeFragment                          -> R.id.homeFragment
                R.id.sectionsFragment, R.id.topicsFragment,
                R.id.topicFragment                        -> R.id.sectionsFragment
                R.id.chatFragment                         -> R.id.chatFragment
                else                                      -> return@addOnDestinationChangedListener
            }
            binding.bottomNavigation.menu.findItem(menuItemId)?.isChecked = true
        }
    }

    private fun setupBottomNavigation() {
        val popToHomeOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setPopUpTo(R.id.homeFragment, inclusive = false, saveState = false)
            .build()

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val currentId = navController.currentDestination?.id
            when (item.itemId) {
                R.id.homeFragment -> {
                    if (currentId != R.id.homeFragment) {
                        navController.navigate(R.id.homeFragment, null, popToHomeOptions)
                    }
                    true
                }
                R.id.sectionsFragment -> {
                    if (currentId != R.id.sectionsFragment) {
                        navController.navigate(R.id.sectionsFragment, null, popToHomeOptions)
                    }
                    true
                }
                R.id.chatFragment -> {
                    if (currentId != R.id.chatFragment) {
                        // Navigate to chat with null args (free mode) — no Safe Args needed
                        val args = Bundle().apply {
                            putString("topicId", null)
                            putString("topicTitle", null)
                            putString("sectionTitle", null)
                        }
                        navController.navigate(
                            R.id.chatFragment, args,
                            NavOptions.Builder().setLaunchSingleTop(true).build()
                        )
                    }
                    true
                }
                else -> false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()
}
