package jp.techacademy.ken.suzuki.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private Toolbar mToolbar;
    private int mGenre = 0;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mGenreRef;
    private DatabaseReference mlikeRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    private QuestionsListAdapter mAdapter;

    // インスタンス変数として、お気に入り一覧のuidを保持するArrayListを定義
    private ArrayList<Question> qaLikeArrayList = new ArrayList<Question>();

    // ログイン済みのユーザーを取得する
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    // Firebaseを参照
    DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
    // Firebaseからユーザーにお気に入りされているUIDを likeRef 変数に代入。
    final DatabaseReference likeRef = dataBaseReference.child(Const.LikesPATH).child(user.getUid());

    // データに追加・変化があった時に受け取るChildEventListener
    private ChildEventListener mFavoriteListener = new ChildEventListener() {
        @Override
        // onChildAddedメソッドが要素が追加されたとき、つまりお気に入りした質問が追加された時に呼ばれるメソッド
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            // datebaseにあるデータの値をHashMapクラスに変換し変数に代入
            HashMap likemap = (HashMap) dataSnapshot.getValue();

            // インスタンス変数として、お気に入り一覧のuidを保持するArrayListを定義
            ArrayList<String> mLikeArrayList = new ArrayList<String>();

            // likeRefに対するリスナーで、mLikeArrayListにお気に入り一覧のuidをadd
            HashMap likelistMap = (HashMap) likemap.get("likes");
            if (likelistMap != null) {
                for (Object key : likelistMap.keySet()) {
                    HashMap liketemp = (HashMap) likelistMap.get((String) key);
                    String likeUid = (String) liketemp.get("uid");
                    mLikeArrayList.add(likeUid);
                }
            }

            // 全ての質問を確認
            for (Question likeQAlist: mQuestionArrayList) {
                // 全ての質問にお気に入りのUidが含まれていれば
                if (mLikeArrayList.contains(likeQAlist.getQuestionUid())) {
                    // 新しいリストの追加
                    qaLikeArrayList.add(likeQAlist);
                }
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener mLikeListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            for (Object question : map.values()) {
                HashMap q = (HashMap) question;
                String title = (String) q.get("title");
                String body = (String) q.get("body");
                String name = (String) q.get("name");
                String uid = (String) q.get("uid");
                String imageString = (String) q.get("image");
                byte[] bytes;
                if (imageString != null) {
                    bytes = Base64.decode(imageString, Base64.DEFAULT);
                } else {
                    bytes = new byte[0];
                }


                ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
                HashMap answerMap = (HashMap) q.get("answers");
                if (answerMap != null) {
                    for (Object key : answerMap.keySet()) {
                        HashMap temp = (HashMap) answerMap.get((String) key);
                        String answerBody = (String) temp.get("body");
                        String answerName = (String) temp.get("name");
                        String answerUid = (String) temp.get("uid");
                        Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                        answerArrayList.add(answer);
                    }
                }

                Question likes = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
                mQuestionArrayList.add(likes);
                mAdapter.setQuestionArrayList(qaLikeArrayList);
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            // 変更があったQuestionを探す
            for (Question question: mQuestionArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {
                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.getAnswers().clear();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    private ChildEventListener mEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                HashMap map = (HashMap) dataSnapshot.getValue();

                    String title = (String) map.get("title");
                    String body = (String) map.get("body");
                    String name = (String) map.get("name");
                    String uid = (String) map.get("uid");
                    String imageString = (String) map.get("image");
                    byte[] bytes;
                    if (imageString != null) {
                        bytes = Base64.decode(imageString, Base64.DEFAULT);
                    } else {
                        bytes = new byte[0];
                    }

                    ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            answerArrayList.add(answer);
                        }
                    }

                    Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
                    mQuestionArrayList.add(question);
                    mAdapter.notifyDataSetChanged();
            }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            // 変更があったQuestionを探す
            for (Question question: mQuestionArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {
                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.getAnswers().clear();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
                if (mGenre == 0) {
                    Snackbar.make(view, "ジャンルを選択して下さい", Snackbar.LENGTH_LONG).show();
                    return;
                }

                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // ジャンルを渡して質問作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), QuestionSendActivity.class);
                    intent.putExtra("genre", mGenre);
                    startActivity(intent);
                }

            }
        });

        // ナビゲーションドロワーの設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.notifyDataSetChanged();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Questionのインスタンスを渡して質問詳細画面を起動する
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 1:趣味を既定の選択とする
        if(mGenre == 0) {
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            onNavigationItemSelected(navigationView.getMenu().getItem(0));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_hobby) {
            mToolbar.setTitle("趣味");
            mGenre = 1;
        } else if (id == R.id.nav_life) {
            mToolbar.setTitle("生活");
            mGenre = 2;
        } else if (id == R.id.nav_health) {
            mToolbar.setTitle("健康");
            mGenre = 3;
        } else if (id == R.id.nav_compter) {
            mToolbar.setTitle("コンピューター");
            mGenre = 4;
        } else if (id == R.id.nav_like) {
            mToolbar.setTitle("お気に入り一覧");
            mGenre = 5;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mQuestionArrayList.clear();
        mAdapter.setQuestionArrayList(mQuestionArrayList);
        mListView.setAdapter(mAdapter);

        // 選択したジャンルにリスナーを登録する
        if (mGenreRef != null) {
            mGenreRef.removeEventListener(mEventListener);
        }

        if (mGenre < 5){
            mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
            mGenreRef.addChildEventListener(mEventListener);
            }
            else {
            // お気に入りした質問にリスナーを登録
            likeRef.addChildEventListener(mFavoriteListener);
            mlikeRef = mDatabaseReference.child(Const.ContentsPATH);
            mlikeRef.addChildEventListener(mLikeListener);
        }

        return true;
    }
}