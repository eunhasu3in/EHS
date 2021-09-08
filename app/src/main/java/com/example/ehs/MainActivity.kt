package com.example.ehs

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.example.ehs.Closet.*
import com.example.ehs.Closet.ClothesSaveActivity.Companion.clothesSaveActivity_Dialog
import com.example.ehs.Closet.CodySaveActivity.Companion.codysaveActivity_Dialog
import com.example.ehs.Fashionista.AutoPro
import com.example.ehs.Fashionista.FashionistaFragment
import com.example.ehs.Fashionista.FashionistaUser_Request
import com.example.ehs.Fashionista.FavoriteCheck_Request
import com.example.ehs.Feed.FeedFragment
import com.example.ehs.Home.AutoHome
import com.example.ehs.Home.HomeFragment
import com.example.ehs.Login.AutoLogin
import com.example.ehs.Mypage.ClothesColor_Response
import com.example.ehs.Mypage.MypageFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_closet.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    val TAG: String = "메인페이지"
    companion object {
        var mContext: Context? = null
        var color_Dialog : ProgressDialog? = null

        var closetInt : Int? = null
    }

    lateinit var getLatitude : String
    lateinit var getLongitude : String
    lateinit var city: String

    private val REQUEST_ACCESS_FINE_LOCATION = 1000

    // 메인액티비티 클래스가 가지고 있는 멤버들
    private lateinit var homeFragment: HomeFragment
    private lateinit var fashionistaFragment: FashionistaFragment
    private lateinit var closetFragment: ClosetFragment
    private lateinit var feedFragment: FeedFragment
    private lateinit var mypageFragment: MypageFragment


    var userId: String? = ""
    val bundle = Bundle()

    private var backKeyPressedTime: Long = 0



    override fun onBackPressed() {
        //super.onBackPressed();
        // 기존 뒤로 가기 버튼의 기능을 막기 위해 주석 처리 또는 삭제

        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간에 2.5초를 더해 현재 시간과 비교 후
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간이 2.5초가 지났으면 Toast 출력
        // 2500 milliseconds = 2.5 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 1500) {
            backKeyPressedTime = System.currentTimeMillis()
            Toast.makeText(this, "뒤로 가기 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_LONG).show()
            return
        }
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간에 2.5초를 더해 현재 시간과 비교 후
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간이 2.5초가 지나지 않았으면 종료
        if (System.currentTimeMillis() <= backKeyPressedTime + 1500) {
            moveTaskToBack(true)
            finish()

        }
    }

    // 화면이 메모리에 올라갔을 때
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AndroidThreeTen.init(this)
        mContext = this

        userId = AutoLogin.getUserId(this)

        // 바텀 네비게이션
        bottom_nav.setOnNavigationItemSelectedListener(onBottomNavItemSeletedListener)

        homeFragment = HomeFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.fragments_frame, homeFragment)
            .commit() // add는 프레그먼트 추가해주는 것



        //권한설정
        setPermission()
        setLocation_Permission()



        swipe_refresh.setOnRefreshListener {
            //새로고침 작업 실행
            Log.d("당겨서", "새로고침")

            //업데이트끝 동그뱅이 없어지게함
            swipe_refresh.isRefreshing = false
        }

        FashionistaUser()
        favorite_check()
        ClosetImg()
        CodyImg()
    }


    // 바텀 네비게이션 아이템 클릭 리스너 설정
    private val onBottomNavItemSeletedListener =
        BottomNavigationView.OnNavigationItemSelectedListener {
            // when은 코틀린에서 switch문
            when (it.itemId) {
                R.id.menu_home -> {
                    Log.d(TAG, "MainActivity - 홈버튼 클릭!")
                    homeFragment = HomeFragment.newInstance()
                    replaceFragment(homeFragment)
                }
                R.id.menu_fashionista -> {
                    Log.d(TAG, "MainActivity - 패셔니스타 버튼 클릭!")

                    //여기에다 실행하면 처음에 안뜸
                    FashionistaUser()
                    favorite_check()
                    fashionistaFragment = FashionistaFragment.newInstance()
                    replaceFragment(fashionistaFragment)
                }
                R.id.menu_closet -> {
                    Log.d(TAG, "MainActivity - 옷장 버튼 클릭!")

                    ClosetImg()
                    CodyImg()
                    closetFragment = ClosetFragment.newInstance()
                    replaceFragment(closetFragment)
                    closetFragment.arguments = bundle

                }
                R.id.menu_feed -> {
                    Log.d(TAG, "MainActivity - 피드 버튼 클릭!")
                    feedFragment = FeedFragment.newInstance()
                    replaceFragment(feedFragment)
                }
                R.id.menu_mypage -> {
                    Log.d(TAG, "MainActivity - 마이페이지 버튼 클릭!")

                    color_Dialog = ProgressDialog(this)
                    color_Dialog?.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    color_Dialog?.setMessage("업로드 중입니다.")
                    color_Dialog?.setCanceledOnTouchOutside(false)
                    color_Dialog?.show()

                    getColor()




                    mypageFragment = MypageFragment.newInstance()
                    replaceFragment(mypageFragment)

                }
            } // when문 끝
            true
        }


    fun replaceFragment(fragment: Fragment?) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragments_frame, fragment!!)
        fragmentTransaction.commit()
    }

    /**
     * 테드 퍼미션 설정
     */
    private fun setPermission() {
        val permission = object : PermissionListener {

            override fun onPermissionGranted() { // 설정해놓은 위험 권한들이 허용되었을 경우 이곳을 수행함.
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) { // 설정해놓은 위험 권한들 중 거부를 한 경우 이곳을 수행함.
                Toast.makeText(this@MainActivity, "권한이 거부 되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        TedPermission.with(this)
            .setPermissionListener(permission)
//            .setRationaleMessage("카메라 앱을 사용하시려면 권한을 허용해주세요.")
            .setDeniedMessage("권한을 거부하셨습니다. [앱 설정] -> [권한] 항목에서 허용해주세요.")
            .setPermissions(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            ).check()
    }


    private fun setLocation_Permission() {
        // OS가 Marshmallow 이상일 경우 권한체크를 해야 합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionCheck =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {

                // 권한 없음
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_ACCESS_FINE_LOCATION
                )
            } else {
                getLocation()
                // ACCESS_FINE_LOCATION 에 대한 권한이 이미 있음.
            }
        } else {

        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // grantResults[0] 거부 -> -1
        // grantResults[0] 허용 -> 0 (PackageManager.PERMISSION_GRANTED)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // ACCESS_FINE_LOCATION 에 대한 권한 획득.
            Log.d("권한", "4")
            getLocation()
        } else {
            // ACCESS_FINE_LOCATION 에 대한 권한 거부.
            Log.d("권한", "5")
        }
    }


    fun getLocation() {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var isGPSEnabled: Boolean = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        var isNetworkEnabled: Boolean = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)


        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )

        } else {

            when { //프로바이더 제공자 활성화 여부 체크
                isNetworkEnabled -> {
                    val location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) //인터넷기반으로 위치를 찾음
                    getLongitude = location?.longitude.toString()
                    getLatitude = location?.latitude.toString()

                    AutoHome.setLongitude(this, getLongitude)
                    AutoHome.setLatitude(this, getLatitude)


                    Log.d(
                        "호롤",
                        "죽여라" + "위도" + getLatitude + "경도" + getLongitude + "zz" + gpsLocationListener
                    )

                    val mGeoCoder = Geocoder(applicationContext, Locale.KOREAN)
                    var mResultList: List<Address>? = null
                    try {
                        mResultList = mGeoCoder.getFromLocation(location?.latitude!!,
                            location?.longitude!!,
                            1)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    if (mResultList != null) {
                        // 내 주소 가져오기
                        city = mResultList[0].getAddressLine(0)
                        Log.d("MainActivity 내 주소 ", mResultList[0].getAddressLine(0))
                        var cutting = city?.split(' ') // 공백을 기준으로 리스트 생성해서 필요한 주소값만 출력하기
                        city = cutting[1] + " " + cutting[2] + " " + cutting[3]
                        AutoHome.setLocation(this@MainActivity, city)

                    }
                }
                isGPSEnabled -> {
                    val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) //GPS 기반으로 위치를 찾음
                    getLongitude = location?.longitude.toString()
                    getLatitude = location?.latitude.toString()

                    Toast.makeText(this, "현재위치를 불러옵니다.", Toast.LENGTH_SHORT).show()
                    Log.d("호롤", "죽여라" + "위도" + getLatitude + "경도" + getLongitude)

                    val mGeoCoder = Geocoder(applicationContext, Locale.KOREAN)
                    var mResultList: List<Address>? = null
                    try {
                        mResultList = mGeoCoder.getFromLocation(
                            location?.latitude!!,
                            location?.longitude!!,
                            1
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    if (mResultList != null) {
                        // 내 주소 가져오기
                        city = mResultList[0].getAddressLine(0)
                        Log.d("내 주소 ", mResultList[0].getAddressLine(0))
                        var cutting = city?.split(' ') // 공백을 기준으로 리스트 생성해서 필요한 주소값만 출력하기
                        city = cutting[1] + " " + cutting[2] + " " + cutting[3]
                        AutoHome.setLocation(this@MainActivity, city)

                    }
                }

            }

        }
    }

    val gpsLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val provider: String = location.provider
            val longitude: Double = location.longitude
            val latitude: Double = location.latitude
            val altitude: Double = location.altitude
        }

        //아래 3개함수는 형식상 필수부분
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }


    /**
     * 전문가 리스트 출력
     */
    fun FashionistaUser() {

        var fuserId: String
        var fuserLevel: String
        var fuserProfileImg : String


        var fuserIdArr = mutableListOf<String>()
        var fuserLevelArr = mutableListOf<String>()
        var fuserProImgArr = mutableListOf<String>()

        val responseListener: Response.Listener<String?> = object : Response.Listener<String?> {
            override fun onResponse(response: String?) {
                try {

                    var jsonObject = JSONObject(response)

                    val arr: JSONArray = jsonObject.getJSONArray("response")

                    for (i in 0 until arr.length()) {
                        val fuserObject = arr.getJSONObject(i)

                        fuserId = fuserObject.getString("userId")
                        fuserLevel = fuserObject.getString("userLevel")
                        fuserProfileImg = fuserObject.getString("userProfileImg")
//                        fuserProfile = AutoLogin.StringToBitmap(fuserProfileImg)

                        fuserIdArr.add(fuserId)
                        fuserLevelArr.add(fuserLevel)
                        fuserProImgArr.add(fuserProfileImg)

                        AutoPro.setProuserId(this@MainActivity, fuserIdArr as ArrayList<String>)
                        AutoPro.setProuserLevel(this@MainActivity,
                            fuserLevelArr as ArrayList<String>)
                        AutoPro.setProuserProImg(this@MainActivity,
                            fuserProImgArr as ArrayList<String>)

//                        var fashin = Fashionista(fuserId, fuserLevel, fuserProfile)
//                        FashionistaList.add(fashin)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
        val fashionistaUserRequest = FashionistaUser_Request(responseListener)
        val queue = Volley.newRequestQueue(this)
        queue.add(fashionistaUserRequest)


    }


    fun ClosetImg() {

        closetInt = 0

        var cuserId: String
        var cclothesName: String
        var clothesArr = mutableListOf<String>()

        val responseListener: Response.Listener<String?> =
            Response.Listener<String?> { response ->
                try {

                    var jsonObject = JSONObject(response)
                    var response = jsonObject.toString()

                    val arr: JSONArray = jsonObject.getJSONArray("response")


                    for (i in 0 until arr.length()) {
                        val clothesObject = arr.getJSONObject(i)
                        cuserId = clothesObject.getString("userId")
                        cclothesName = clothesObject.getString("clothesName")


                        clothesArr.add(cclothesName)

                        AutoCloset.setClothesName(this, clothesArr as ArrayList<String>)
                        Log.d("ㅁㅁㅁㅁㅁ메인함수", clothesArr.toString())
                        Log.d("ㅁㅁㅁㅁㅁ메인함수인트1----", closetInt.toString())
                    }
                    closetInt = 1
                    Log.d("ㅁㅁㅁㅁㅁ메인함수인트", closetInt.toString())
                    clothesSaveActivity_Dialog?.dismiss()

                    if(ClothesSaveActivity.clothesSaveContext!=null) {
                        (ClothesSaveActivity.clothesSaveContext as ClothesSaveActivity).finish()
                    }


                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        val closetServer_Request = ClosetServer_Request(userId!!, responseListener)
        val queue = Volley.newRequestQueue(this)
        queue.add(closetServer_Request)
    }

    fun CodyImg() {

        var cuserId: String
        var codyImgName: String
        var codyArr = mutableListOf<String>()

        val responseListener: Response.Listener<String?> =
            Response.Listener<String?> { response ->
                try {

                    var jsonObject = JSONObject(response)
                    var response = jsonObject.toString()

                    val arr: JSONArray = jsonObject.getJSONArray("response")

                    for (i in 0 until arr.length()) {
                        val codyObject = arr.getJSONObject(i)

                        cuserId = codyObject.getString("userId")
                        codyImgName = codyObject.getString("codyImgName")

                        codyArr.add(codyImgName)

                        AutoCody.setCodyName(this, codyArr as ArrayList<String>)

                    }
                    codysaveActivity_Dialog?.dismiss()


                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        val codyServer_Request = CodyServer_Request(userId!!, responseListener)
        val queue = Volley.newRequestQueue(this)
        queue.add(codyServer_Request)
    }

    fun favorite_check() {

        var cuserId: String
        var favoriteuserId: String
        var favorite_true: String
        var favoriteuserIdArr = mutableListOf<String>()

        val responseListener: Response.Listener<String?> =
            Response.Listener<String?> { response ->
                try {

                    var jsonObject = JSONObject(response)
                    var response = jsonObject.toString()

                    val arr: JSONArray = jsonObject.getJSONArray("response")

                    Log.d("기분크기", arr.length().toString())

                    for (i in 0 until arr.length()) {
                        val Object = arr.getJSONObject(i)

                        cuserId = Object.getString("userId")
                        favoriteuserId = Object.getString("prouserId")
                        favorite_true = Object.getString("favorite_true")

                        favoriteuserIdArr.add(favoriteuserId)
                        Log.d("기분?", favoriteuserId)

                        AutoPro.setFavoriteuserId(this,
                            favoriteuserIdArr as java.util.ArrayList<String>)

                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        val favoritecheck_Request = FavoriteCheck_Request(userId!!, responseListener)
        val queue = Volley.newRequestQueue(this)
        queue.add(favoritecheck_Request)
    }

    fun getColor() {
        var cColor : String
        var cCnt : String
        var cColorArr = mutableListOf<String>()
        var cCntArr = mutableListOf<String>()

        val responseListener: Response.Listener<String?> = object : Response.Listener<String?> {
            override fun onResponse(response: String?) {
                try {

                    var jsonObject = JSONObject(response)

                    val arr: JSONArray = jsonObject.getJSONArray("response")

                    for (i in 0 until arr.length()) {
                        val proObject = arr.getJSONObject(i)

                        cColor = proObject.getString("clothesColor")
                        cCnt = proObject.getString("cnt")

                        cColorArr.add(cColor)
                        cCntArr.add(cCnt)

                        Log.d("유저색", cColor)
                        Log.d("유저색갯수", cCnt)
                        AutoCloset.setClothesColor(this@MainActivity, cColorArr as ArrayList<String>)
                        AutoCloset.setColorCnt(this@MainActivity, cCntArr as ArrayList<String>)
                    }


                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
        val clothescolorResponse = ClothesColor_Response(userId!!, responseListener)
        val queue = Volley.newRequestQueue(this)
        queue.add(clothescolorResponse)

    }



}