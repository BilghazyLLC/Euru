package io.euruapp.view

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntroFragment
import com.github.paolorotolo.appintro.model.SliderPage
import io.euruapp.R
import io.euruapp.util.ConstantsUtils
import io.euruapp.viewmodel.UserDatabase

/**
 * [io.euruapp.model.User] onboarding screen
 * Educates the user on how to use the application
 */
class OnboardingActivity : AppIntro2() {
    private lateinit var database: UserDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = UserDatabase(this@OnboardingActivity)

        //Create Sliders for each fragment
        addSlide(AppIntroFragment.newInstance(getSlider(0)))
        addSlide(AppIntroFragment.newInstance(getSlider(1)))
//        addSlide(AppIntroFragment.newInstance(getSlider(2)))
//        addSlide(AppIntroFragment.newInstance(getSlider(3)))

        showSkipButton(true)
        setFlowAnimation()
        //setBackgroundView(LayoutInflater.from(this).inflate(R.layout.custom_background,null,false));
    }

    private fun getSlider(position: Int): SliderPage {
        val title: String
        val desc: String
        val drawable: Int


        //Changes the color of the background of the activity
        val bgColor = ContextCompat.getColor(this, R.color.designer_news_link_highlight)

        val bgColorTwo = ContextCompat.getColor(this,R.color.indigo)
        val bgColorThree = ContextCompat.getColor(this,R.color.green)
        val bgColorFour = ContextCompat.getColor(this,R.color.yellow)


        val titleColor = ContextCompat.getColor(this, R.color.white)
        val descColor = ContextCompat.getColor(this, R.color.white)

        // TODO: 10/20/2018 Setup onboarding screen with recommended images, title & description
        when (position) {
            0 -> {
                drawable = R.drawable.app_logo
                title = "Welcome to Euru\n"
                desc = "--Login as a customer to view service categories\n--Send a request to service providers\n--Call service providers from a button click\n--Receive push notifications\n--See your pending jobs in real-time"
                return SliderPage(title, desc, drawable, bgColor, titleColor, descColor)
            }
            else -> {
                title = "Features worth mentioning"
                desc = "--Register as a service provider\n --Provide necessary particulars required\n --Indicate your service category \n --And just relax whilst Euru links you to customers"
                drawable = R.drawable.joinlines
                return SliderPage(title, desc, drawable, bgColorTwo, titleColor, descColor)
            }
           /* 2 -> {
                title = ""
                desc = ""
                drawable = R.drawable.welcome
                return SliderPage(title, desc, drawable, bgColorThree, titleColor, descColor)
            }
            else -> {
                title = ""
                desc = ""
                drawable = R.drawable.welcome
                return SliderPage(title, desc, drawable, bgColorFour, titleColor, descColor)
            }*/
        }
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        ConstantsUtils.intentTo(this@OnboardingActivity, if (database.isLoggedIn) HomeActivity::class.java else AuthActivity::class.java)
        finishAfterTransition()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        ConstantsUtils.intentTo(this@OnboardingActivity, if (database.isLoggedIn) HomeActivity::class.java else AuthActivity::class.java)
        finishAfterTransition()
    }


}
