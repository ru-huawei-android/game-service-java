package com.huawei.gameservice;

import android.content.Intent;
import android.os.*;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.jos.JosApps;
import com.huawei.hms.jos.JosAppsClient;
import com.huawei.hms.jos.games.*;
import com.huawei.hms.jos.games.archive.ArchiveDetails;
import com.huawei.hms.jos.games.archive.ArchiveSummary;
import com.huawei.hms.jos.games.archive.ArchiveSummaryUpdate;
import com.huawei.gameservice.R;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.result.HuaweiIdAuthResult;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button login;
    Button save;
    Button load;
    TextView text;
    EditText edit;

    Integer SIGN_IN_INTENT = 3000;
    Integer level = 101;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    public void initView() {
        login = findViewById(R.id.login);
        login.setOnClickListener(this);
        save = findViewById(R.id.save);
        save.setOnClickListener(this);
        load = findViewById(R.id.load);
        load.setOnClickListener(this);
        text = findViewById(R.id.text);
        edit = findViewById(R.id.edit);
    }

    public void init() {
        JosAppsClient appsClient = JosApps.getJosAppsClient(this, SignInCenter.get().getAuthHuaweiId());
        appsClient.init();
    }

    public HuaweiIdAuthParams getHuaweiIdParams() {
        return new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME).createParams();
    }

    public void signIn() {
        Task<AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager.getService(this, getHuaweiIdParams()).silentSignIn();
        authHuaweiIdTask.addOnSuccessListener(authHuaweiId ->
                SignInCenter.get().updateAuthHuaweiId(authHuaweiId)
        ).addOnFailureListener(e -> {
            if (e instanceof ApiException) {
                signInNewWay();
            }
        });
    }

    public void signInNewWay() {
        Intent intent = HuaweiIdAuthManager.getService(MainActivity.this, getHuaweiIdParams()).getSignInIntent();
        startActivityForResult(intent, SIGN_IN_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (SIGN_IN_INTENT == requestCode) {
            handleSignInResult(data);
        }
    }

    private void handleSignInResult(Intent data) {
        if (null == data) {
            return;
        }
        String jsonSignInResult = data.getStringExtra("HUAWEIID_SIGNIN_RESULT");
        try {
            HuaweiIdAuthResult
                    signInResult = new HuaweiIdAuthResult
                    ().fromJson(jsonSignInResult);
            if (0 == signInResult.getStatus().getStatusCode()) {
                SignInCenter.get().updateAuthHuaweiId(signInResult.getHuaweiId());
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.onfail) + signInResult.getStatus().getStatusCode(),Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.load:
                this.getArchive();
                break;
            case R.id.save:
                this.clearArchives();
                break;
            case R.id.login:
                this.init();
                this.signIn();
                break;
            default:
                break;
        }
    }

    private void getArchive() {
        Task<List<ArchiveSummary>> task = Games.getArchiveClient(this, SignInCenter.get().getAuthHuaweiId()).getArchiveSummaryList(true);
        task.addOnSuccessListener(archiveSummaries -> {
            ArchiveSummary archiveSummary = archiveSummaries.get(0);
            text.setText(getString(R.string.time, archiveSummary.getActiveTime()));
        }).addOnFailureListener(e -> {
        });
    }

    private void addArchive() {
        String description = getString(R.string.description);
        long playedTime = Integer.parseInt(edit.getText().toString());
        long progress = Math.round(level * Math.random());
        ArchiveSummaryUpdate.Builder builder = new ArchiveSummaryUpdate.Builder().setActiveTime(playedTime)
                .setCurrentProgress(progress)
                .setDescInfo(description);
        ArchiveSummaryUpdate archiveMetadataChange = builder.build();
        ArchiveDetails archiveContents = new ArchiveDetails.Builder().build();
        archiveContents.set((progress + description + playedTime).getBytes());
        Task<ArchiveSummary> task = Games.getArchiveClient(this, SignInCenter.get().getAuthHuaweiId()).addArchive(archiveContents, archiveMetadataChange, true);
        task.addOnSuccessListener(archiveSummary -> {
            Toast.makeText(getApplicationContext(), getString(R.string.onsaved),Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getApplicationContext(), getString(R.string.onfail),Toast.LENGTH_SHORT).show();
        });
    }

    private void clearArchives() {
        Task<List<ArchiveSummary>> task = Games.getArchiveClient(this, SignInCenter.get().getAuthHuaweiId()).getArchiveSummaryList(true);
        task.addOnSuccessListener(archiveSummaries -> {
            if (archiveSummaries != null) {
                for (int i = 0; i < archiveSummaries.size(); i++) {
                    Task<String> task1 = Games.getArchiveClient(this, SignInCenter.get().getAuthHuaweiId()).removeArchive(archiveSummaries.get(i));
                    task1.addOnSuccessListener(id -> {
                        if (id.equals(archiveSummaries.get(archiveSummaries.size() - 1).getId()))
                            addArchive();
                    }).addOnFailureListener(e -> {
                    });
                }
            } else {
                addArchive();
            }
        }).addOnFailureListener(e -> {
        });
    }
}


