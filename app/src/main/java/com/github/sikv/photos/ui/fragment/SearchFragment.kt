package com.github.sikv.photos.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.github.sikv.photos.R
import com.github.sikv.photos.enumeration.SearchSource
import com.github.sikv.photos.util.changeVisibilityWithAnimation
import com.github.sikv.photos.util.hideSoftInput
import com.github.sikv.photos.util.showSoftInput
import com.github.sikv.photos.util.showToolbarBackButton
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : BaseFragment() {

    companion object {
        private const val EXTRA_SEARCH_TEXT = "extra_search_text"
        private const val EXTRA_LAST_SEARCH_TEXT = "extra_last_search_text"

        fun newInstance(searchText: String? = null): SearchFragment {
            val fragment = SearchFragment()

            searchText?.let {
                val args = Bundle()
                args.putString(EXTRA_SEARCH_TEXT, it)

                fragment.arguments = args
            }

            return fragment
        }
    }

    override val overrideBackground: Boolean = true

    private lateinit var viewPagerAdapter: SearchViewPagerAdapter

    private var lastSearchText: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showToolbarBackButton {
            navigation?.backPressed()
        }

        initViewPager {
            if (savedInstanceState == null) {
                arguments?.getString(EXTRA_SEARCH_TEXT)?.let { searchText ->
                    searchEdit.append(searchText)
                    searchPhotos(searchText)
                }

                shownKeyboardIfNeeded()
            }
        }

        setListeners()

        changeClearButtonVisibility(false, withAnimation = false)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState != null) {
            savedInstanceState.getString(EXTRA_LAST_SEARCH_TEXT)?.let { text ->
                searchPhotos(text)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        lastSearchText?.let { text ->
            outState.putSerializable(EXTRA_LAST_SEARCH_TEXT, text)
        }
    }

    private fun searchPhotos(text: String) {
        context?.hideSoftInput(searchEdit)

        lastSearchText = text

        viewPagerAdapter.searchPhotos(viewPager, text)
    }

    private fun shownKeyboardIfNeeded() {
        val showKeyboard = arguments?.getString(EXTRA_SEARCH_TEXT) == null
        var keyboardShown = false

        if (showKeyboard) {
            if (!keyboardShown) {
                keyboardShown = requireActivity().showSoftInput(searchEdit)
            }

            searchEdit.viewTreeObserver.addOnWindowFocusChangeListener(object : ViewTreeObserver.OnWindowFocusChangeListener {
                override fun onWindowFocusChanged(hasFocus: Boolean) {
                    if (hasFocus && !keyboardShown) {
                        keyboardShown = requireActivity().showSoftInput(searchEdit)
                    }

                    searchEdit?.viewTreeObserver?.removeOnWindowFocusChangeListener(this)
                }
            })
        }
    }

    private fun changeClearButtonVisibility(visible: Boolean, withAnimation: Boolean = true) {
        val newVisibility = if (visible) View.VISIBLE else View.INVISIBLE

        if (searchClearButton.visibility != newVisibility) {
            if (withAnimation) {
                searchClearButton.changeVisibilityWithAnimation(newVisibility)
            } else {
                searchClearButton.visibility = newVisibility
            }
        }
    }

    private fun initViewPager(after: () -> Unit) {
        viewPagerAdapter = SearchViewPagerAdapter(childFragmentManager)

        viewPager.adapter = viewPagerAdapter

        tabLayout.setupWithViewPager(viewPager)

        viewPager.post {
            after()
        }
    }

    private fun setListeners() {
        searchEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchPhotos(searchEdit.text.toString())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        searchEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                changeClearButtonVisibility(editable?.isNotEmpty() ?: false)
            }

            override fun beforeTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        searchClearButton.setOnClickListener {
            searchEdit.text?.clear()
            context?.showSoftInput(searchEdit)
        }
    }

    /**
     * SearchViewPagerAdapter
     */

    private class SearchViewPagerAdapter(
            fragmentManager: FragmentManager
    ) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return SingleSearchFragment.newInstance(SearchSource.values()[position].photoSource)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return SearchSource.values()[position].photoSource.title
        }

        override fun getCount(): Int {
            return SearchSource.values().size
        }

        fun searchPhotos(viewPager: ViewPager, text: String) {
            startUpdate(viewPager)

            for (i in 0 until count) {
                (instantiateItem(viewPager, i) as? SingleSearchFragment)?.searchPhotos(text)
            }

            finishUpdate(viewPager)
        }
    }
}