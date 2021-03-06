package info.devram.dainikhatabook;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import info.devram.dainikhatabook.Adapters.DashBoardRecyclerAdapter;
import info.devram.dainikhatabook.Adapters.RecyclerOnClick;
import info.devram.dainikhatabook.Entities.AccountEntity;
import info.devram.dainikhatabook.ErrorHandlers.ApplicationError;
import info.devram.dainikhatabook.ErrorHandlers.LogError;
import info.devram.dainikhatabook.Helpers.Config;
import info.devram.dainikhatabook.Interfaces.FileErrorLoggerListener;
import info.devram.dainikhatabook.Services.NotifyService;
import info.devram.dainikhatabook.ViewModel.AccountViewModel;
import info.devram.dainikhatabook.ui.ConfirmModal;

public class DetailActivity extends AppCompatActivity
        implements RecyclerOnClick, ConfirmModal.ConfirmModalListener, FileErrorLoggerListener {

    //private static final String TAG = "DetailActivity";

    public static final int EDIT_EXP_REQUEST_CODE = 1;
    public static final int EDIT_INC_REQUEST_CODE = 2;

    private AccountViewModel accountViewModel;
    private RecyclerView recyclerView;
    private DashBoardRecyclerAdapter dashBoardRecyclerAdapter;
    private List<AccountEntity> accountList;
    private int itemAdapterPosition;
    private String intentType;
    private boolean hasExpense = false;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            Toolbar toolbar = findViewById(R.id.detailToolbar);

            if (toolbar != null) {
                setSupportActionBar(toolbar);
                actionBar = getSupportActionBar();
            }
        }

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        hasExpense = getIntent().hasExtra(Config.EXPENSE_TABLE_NAME);
        accountViewModel = AccountViewModel.getInstance(getApplication());

        accountViewModel.init();

        recyclerView = findViewById(R.id.detailRecycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        intentType = getIntent().getStringExtra("type");

        inflateDashBoardList();

        dashBoardRecyclerAdapter = new DashBoardRecyclerAdapter(accountList, this);

        recyclerView.setAdapter(dashBoardRecyclerAdapter);
    }

    @Override
    public void onItemClicked(View view, int position) {

        switch (view.getId()) {
            case R.id.detailEditBtn:
                Intent intent = new Intent(DetailActivity.this, EditActivity.class);

                itemAdapterPosition = position;

                AccountEntity accountEntity = accountList.get(position);

                if (hasExpense) {

                    intent.putExtra(Config.EXPENSE_TABLE_NAME, accountEntity);
                    startActivityForResult(intent, EDIT_EXP_REQUEST_CODE);
                } else {

                    intent.putExtra(Config.INCOME_TABLE_NAME, accountEntity);
                    startActivityForResult(intent, EDIT_INC_REQUEST_CODE);
                }

                break;
            case R.id.detailDeleteBtn:
                itemAdapterPosition = position;
                ConfirmModal confirmModal = new
                        ConfirmModal("Are You Sure You Want To Delete This",
                        "Alert!" + "\n\n", true, this);
                confirmModal.show(getSupportFragmentManager(), null);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_EXP_REQUEST_CODE) {
            if (resultCode == 1) {
                if (data != null) {
                    AccountEntity accountEntity = (AccountEntity) data
                            .getSerializableExtra(Config.EXPENSE_TABLE_NAME);

                    try {
                        accountViewModel.editAccount(accountEntity);
                    } catch (ApplicationError error) {
                        this.logErrorToFile(error);
                        NotifyService.createNotification("Inserting Data",
                                "Error Inserting Data...Try Again", this);
                    }
                }
            }
        }
        if (requestCode == EDIT_INC_REQUEST_CODE) {
            if (resultCode == 1) {
                if (data != null) {
                    AccountEntity accountEntity = (AccountEntity) data
                            .getSerializableExtra(Config.INCOME_TABLE_NAME);

                    try {
                        accountViewModel.editAccount(accountEntity);
                    } catch (ApplicationError error) {
                        this.logErrorToFile(error);
                        NotifyService.createNotification("Inserting Data",
                                "Error Inserting Data...Try Again", this);
                    }
                }
            }
        }

    }

    @Override
    public void onOkClick(DialogFragment dialogFragment) {
        AccountEntity accountEntity = accountList.get(itemAdapterPosition);

        try {
            accountViewModel.deleteAccount(accountEntity);

        } catch (ApplicationError error) {
            NotifyService.createNotification("Deleting Data",
                    "Error Deleting Data...Try Again", this);
        }

        inflateDashBoardList();
        dashBoardRecyclerAdapter.updateData(accountList);
        dialogFragment.dismiss();
    }

    @Override
    public void onCancelClick(DialogFragment dialogFragment) {
        dialogFragment.dismiss();
    }

    private void inflateDashBoardList() {

        accountList = new ArrayList<>();

        accountList = accountViewModel.getAccountByType(intentType);

    }

    private void logErrorToFile(ApplicationError error) {
        executorService = Executors.newCachedThreadPool();

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("error", error.getMessage());
        hashMap.put("trace", error.getStackTrace().toString());

        LogError logError = new LogError(hashMap, this, this);

        executorService.execute(logError);
    }

    @Override
    public void fileStatusListener(String status) {
        executorService.shutdown();
    }
}