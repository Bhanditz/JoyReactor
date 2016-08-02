package y2k.joyreactor.common

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import y2k.joyreactor.R
import y2k.joyreactor.services.BroadcastService
import y2k.joyreactor.services.LifeCycleService

/**
 * Created by y2k on 3/4/16.
 */

open class VMActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(getContentView())
        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun getContentView(): Int {
        return javaClass.simpleName
            .replace("Activity", "")
            .let { "activity_" + it.toLowerCase() }
            .let { resources.getIdentifier(it, "layout", packageName) }
    }
}

open class BaseActivity : AppCompatActivity() {

    var menuHolder = MenuHolder()
    val lifeCycleService = LifeCycleService(ServiceLocator.resolve<BroadcastService>())

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)

        setSupportActionBar(find<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return menuHolder.onCreateOptionsMenu(menu, menuInflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return menuHolder.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        lifeCycleService.activate()
    }

    override fun onPause() {
        super.onPause()
        lifeCycleService.deactivate()
    }
}