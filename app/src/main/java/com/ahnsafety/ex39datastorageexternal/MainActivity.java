package com.ahnsafety.ex39datastorageexternal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity {

    EditText et;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et= findViewById(R.id.et);
        tv= findViewById(R.id.tv);
    }

    public void clickSave(View view) {

        //외장메모리(SDcard)가 있는지?
        String state= Environment.getExternalStorageState();

        //외장메모리상태(state)가 연결(mounted)되어 있는지 확인
        if( !state.equals(Environment.MEDIA_MOUNTED) ){
            Toast.makeText(this, "SDcard is not mounted", Toast.LENGTH_SHORT).show();
            return;
        }

        //여기까지 커서가 오면..
        //외장메모리가 연결되어있는 것임.
        // 외부메모리에 데이터 저장 작업 시작

        String data= et.getText().toString();
        et.setText("");

        File path; //파일저장될 경로정보를 가진 객체[Data.txt파일이 저장될 경로]

        //Marshmallow(api 23버전) 이상에서는
        //SD카드의 아무 위치에 직접 저장하는 것이
        //불가능하도록 보안을 강화함(동적퍼미션을 받아야 가능함)
        //오로지 각 앱에게 할당된 고유한 영역에만
        //저장이 가능함.
        // 그 고유한 영역의 경로를 얻어오기!
        File[] dirs= getExternalFilesDirs("MyDir");
        path= dirs[0];
        tv.setText( path.getPath() );

        //위에서 만들어진 경로에 Data.txt문서를
        //만들고 데이터를 저장
        File file= new File(path, "Data.txt");//경로+파일명

        try {
            FileOutputStream fos= new FileOutputStream(file, true);
            PrintWriter writer= new PrintWriter(fos);

            writer.println(data);
            writer.flush();
            writer.close();

            Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    public void clickLoad(View view) {

        //외부메모리가 존재하는지 확인
        String state= Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED) ||
                state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)){

            //읽을 수 있는 상태

            File path= getExternalFilesDirs("MyDir")[0];
            File file= new File(path,"Data.txt");

            try {
                FileReader fr= new FileReader(file);
                BufferedReader reader= new BufferedReader(fr);

                StringBuffer buffer= new StringBuffer();

                String line= reader.readLine();
                while (line!=null){
                    buffer.append(line+"\n");
                    line= reader.readLine();
                }

                tv.setText(buffer.toString());
                reader.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    //requestPermissions()메소드를 실행해서
    //보여진 퍼미션요청 다이얼로그의
    //DENY, ALLOW 버튼 중 하나를 선택하면
    //자동으로 실행되는 콜백메소드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch ( requestCode ){
            case 100:

                //사용자가 선택한 결과가 ALLOW 인가?
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "외부저장소 쓰기 가능", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "거부하셨기에 외부저장소 사용 불가", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    public void clickBtn(View view) {
        //외부메모리의 특정 위치에 저장하려면
        //동적 퍼미션이 필요함.(api23버전 이상부터)

        //외부메모리이 연결되어있는지 확인
        String state= Environment.getExternalStorageState();
        if( !state.equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(this, "외부저장소 없음", Toast.LENGTH_SHORT).show();
            return;
        }

        //동적퍼미션 체크작업(api23버전 이상일때)
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            //이 앱이 사용자로부터 퍼미션을 받았는지 체크
            int checkResult= checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            //퍼미션이 허용되어 있지 않다면?
            if(checkResult == PackageManager.PERMISSION_DENIED){
                //사용자에게 퍼미션을 요청하는 다이얼로그를 보여주는 메소드를 실행
                String[] permissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, 100);

                return;
            }

        }//



        //퍼미션이 허가되었다면
        //외부메모리에 저장하기!!

        //SDcard의 특정 위치에 aaa.txt문서 저장
        File path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if(path!=null) tv.setText(path.getPath());

        File file= new File(path, "aaa.txt");//경로+파일명

        try {
            FileWriter fw= new FileWriter(file, true);
            PrintWriter writer= new PrintWriter(fw);

            writer.println(et.getText().toString());
            writer.flush();
            writer.close();

            et.setText("");
            tv.append("SAVED");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
