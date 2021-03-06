package br.com.caiodev.walletapp.sections.statement.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import br.com.caiodev.walletapp.R
import br.com.caiodev.walletapp.sections.login.model.LoginResponse
import br.com.caiodev.walletapp.sections.login.view.LoginActivity
import br.com.caiodev.walletapp.sections.statement.view_model.StatementViewModel
import br.com.caiodev.walletapp.sections.statement.view_model.ViewModelDataHelper
import br.com.caiodev.walletapp.utils.HawkIds
import br.com.caiodev.walletapp.utils.MasterActivity
import br.com.caiodev.walletapp.utils.extension.deleteHawkValue
import br.com.caiodev.walletapp.utils.extension.getHawkValue
import kotlinx.android.synthetic.main.activity_user_account_detail.*

class UserAccountDetailActivity : MasterActivity(), LifecycleOwner {

    private lateinit var viewModel: StatementViewModel
    private var viewModelDataHelper: ViewModelDataHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_account_detail)

        setupView()
        setupViewModel()
        handleViewModel()
    }

    override fun setupView() {
        setViewVisibility(statementListProgressBar, View.VISIBLE)
        accountOwnerStatementsRecyclerView.setHasFixedSize(true)
        statementListSwipeRefreshLayout.setColorSchemeResources(R.color.purple)
    }

    override fun setupViewModel() {

        viewModel = ViewModelProviders.of(this).get(StatementViewModel::class.java)
        viewModelDataHelper = ViewModelDataHelper()

        viewModel.getHawkValue<LoginResponse>(HawkIds.userLoginResponseData).userAccount.apply {
            accountOwnerNameTextView.text = name
            accountNumberTextView.text = bankAccount

            accountBalanceTextView.text =
                String.format(getString(R.string.balance_placeholder), balance.toString())
        }

        logoutImageView.setOnClickListener {
            viewModel.deleteHawkValue(HawkIds.userLoginResponseData)
            startActivity(Intent(applicationContext, LoginActivity::class.java))
            finish()
        }

        statementListSwipeRefreshLayout.setOnRefreshListener {
            viewModel.getStatement()
        }
    }

    override fun handleViewModel() {

        viewModel.getStatement()

        viewModel.state.observe(this, Observer { state ->

            when (state) {

                StatementViewModel.onStatementRetrievalSuccess -> {

                    viewModelDataHelper?.let {

                        it.listReceiver(viewModel.getStatementList())

                        accountOwnerStatementsRecyclerView.adapter =
                            StatementAdapter(it)

                        runLayoutAnimation(accountOwnerStatementsRecyclerView)
                    }
                }

                StatementViewModel.onStatementRetrievalError -> {
                    setViewVisibility(statementListProgressBar, View.GONE)
                    setViewVisibility(statementListSwipeRefreshLayout)
                    showSnackBar("Check your internet connection")
                }
            }
        })
    }

    private fun runLayoutAnimation(recyclerView: RecyclerView) {

        val controller =
            AnimationUtils.loadLayoutAnimation(
                recyclerView.context,
                R.anim.layout_animation_fall_down
            )

        recyclerView.layoutAnimation = controller
        recyclerView.adapter?.notifyDataSetChanged()
        recyclerView.scheduleLayoutAnimation()

        setViewVisibility(statementListSwipeRefreshLayout)

        if (statementListProgressBar.visibility == View.VISIBLE) setViewVisibility(
            statementListProgressBar,
            View.GONE
        )
    }
}