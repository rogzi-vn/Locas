package vn.vistark.locas.ui.danh_gia

import FavoritePlaces
import PlaceInRangeResult
import UserRatingResponse
import UserRatings
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import vn.vistark.locas.R
import vn.vistark.locas.core.api.APIUtils
import vn.vistark.locas.core.response_model.check.CheckResponse
import vn.vistark.locas.core.utils.LoadingDialog
import vn.vistark.locas.core.utils.SaveFileUtils
import vn.vistark.locas.core.utils.SimpleNotify
import vn.vistark.locas.ui.trashing.TempActivityForSelectFile
import java.io.File
import java.lang.Exception

class DanhGiaDiaLog(context: Context) : AlertDialog(context) {

    val cmtRatings = ArrayList<UserRatings>()
    lateinit var adapter: DanhGiaAdapter

    companion object {
        var current: DanhGiaDiaLog? = null
        var selectedUri: Uri? = null
        var bitmap: Bitmap? = null
        fun updateSelectedUri(uri: Uri?) {
            Log.w("FUCK", "img");
            selectedUri = uri
            if (current != null) {
                Glide.with(current!!.attImg1)
                    .asBitmap()
                    .load(selectedUri)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onLoadCleared(placeholder: Drawable?) {

                        }

                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            Log.w("FUCK", "img2222222222");
                            try {
                                current!!.attImg1.setImageBitmap(resource)
                                bitmap = resource
//                                current!!.attImg1.setImageDrawable(current!!.ivTemp.drawable)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(current!!.context, e.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    })
            } else {
                Log.w("FUCK", "imgzzxzcxxzcxxzc");
            }
        }
    }

    lateinit var loadingDiaLog: LoadingDialog

    init {
        current = this
        initialize()
    }

    constructor(context: Context, fp: FavoritePlaces) : this(context) {
        rbRatingStar.rating = fp.rating
        tvRating.text = String.format("%.01f", fp.rating)
        Glide.with(ivHinhAnhDiaDiem).load(fp.hinh_anh).into(ivHinhAnhDiaDiem)
        Glide.with(ivLogoHinhAnh).load(fp.logo).into(ivLogoHinhAnh)
        tvTenDiaDiem.text = fp.ten_dd
        addRating(fp.ma_dd)
        ivAddLoveBtn.visibility = View.GONE
        loadComments(fp.ma_dd)
    }

    constructor(context: Context, pir: PlaceInRangeResult) : this(context) {
        rbRatingStar.rating = pir.rating
        tvRating.text = String.format("%.01f", pir.rating)
        Glide.with(ivHinhAnhDiaDiem).load(pir.hinh_anh).into(ivHinhAnhDiaDiem)
        Glide.with(ivLogoHinhAnh).load(pir.logo).into(ivLogoHinhAnh)
        tvTenDiaDiem.text = pir.ten_dd
        addRating(pir.ma_dd)
        ivAddLoveBtn.visibility = View.VISIBLE
        addFavorite(pir.ma_dd)
        loadComments(pir.ma_dd)
    }

    private fun addFavorite(maDd: Int) {
        ivAddLoveBtn.setOnClickListener {
            loadingDiaLog.show()
            APIUtils.mAPIServices?.addFavorite(maDd)?.enqueue(object : Callback<CheckResponse> {
                override fun onFailure(call: Call<CheckResponse>, t: Throwable) {
                    SimpleNotify.error(context, "Dường như bạn đã thêm rồi", "")
                    loadingDiaLog.dismiss()
                }

                override fun onResponse(
                    call: Call<CheckResponse>,
                    response: Response<CheckResponse>
                ) {
                    if (response.isSuccessful) {
                        val check = response.body()
                        if (check != null && check.code == 1) {
                            SimpleNotify.success(
                                context,
                                "Đã thêm vào mục yêu thích. Bạn không thể lặp lại thao tác",
                                ""
                            )
                            loadingDiaLog.dismiss()
                            return
                        }
                    }
                    SimpleNotify.error(context, "Dường như bạn đã thêm rồi", "")
                    loadingDiaLog.dismiss()
                    return
                }
            })
        }
    }

    fun addRating(placeId: Int) {
        ivBtnSend.setOnClickListener {
            val rating = cmtRatingNumber.text.toString()
            if (rating.toFloat() < 1F) {
                SimpleNotify.error(context, "Đánh giá nhỏ nhất là 1 sao", "")
                return@setOnClickListener
            }
            val cmt = edtCommentContent.text.toString()
            if (cmt.isEmpty()) {
                SimpleNotify.error(context, "Bạn chưa nhập nội dung đánh giá", "")
                return@setOnClickListener
            }
            if (selectedUri == null) {
                SimpleNotify.error(context, "Bạn phải có ảnh minh họa địa điểm", "")
                return@setOnClickListener
            }

            SaveTempFile(context, placeId, rating.toFloat(), cmt).execute()
        }
    }

    lateinit var ivCloseBtn: ImageView
    lateinit var rbRatingStar: RatingBar
    lateinit var tvRating: TextView
    lateinit var ivHinhAnhDiaDiem: ImageView
    lateinit var ivLogoHinhAnh: CircleImageView
    lateinit var tvTenDiaDiem: TextView
    lateinit var rvUserComments: RecyclerView
    lateinit var cmtRating: RatingBar
    lateinit var cmtRatingNumber: TextView
    lateinit var attImg1: ImageView
    lateinit var edtCommentContent: EditText
    lateinit var ivBtnSend: ImageView
    lateinit var ivAddLoveBtn: ImageView

    fun initViews(v: View) {
        loadingDiaLog = LoadingDialog(context)
        ivCloseBtn = v.findViewById(R.id.ivCloseBtn)
        rbRatingStar = v.findViewById(R.id.rbRatingStar)
        ivHinhAnhDiaDiem = v.findViewById(R.id.ivHinhAnhDiaDiem)
        ivLogoHinhAnh = v.findViewById(R.id.ivLogoHinhAnh)
        tvRating = v.findViewById(R.id.tvRating)
        tvTenDiaDiem = v.findViewById(R.id.tvTenDiaDiem)
        rvUserComments = v.findViewById(R.id.rvUserComments)
        cmtRating = v.findViewById(R.id.cmtRating)
        cmtRatingNumber = v.findViewById(R.id.cmtRatingNumber)
        attImg1 = v.findViewById(R.id.attImg1)
        edtCommentContent = v.findViewById(R.id.edtCommentContent)
        ivBtnSend = v.findViewById(R.id.ivBtnSend)
        ivAddLoveBtn = v.findViewById(R.id.ivAddLoveBtn)
    }

    fun initialize() {
        val v = LayoutInflater.from(context).inflate(R.layout.layout_danh_gia, null)
        initViews(v)
        initEvents()
        initCommentLists()
        setView(v)

        setOnCancelListener {
            selectedUri = null
            bitmap = null
        }

        setCancelable(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        show()
    }

    private fun initCommentLists() {
        rvUserComments.setHasFixedSize(true)
        rvUserComments.layoutManager = LinearLayoutManager(context)
        adapter = DanhGiaAdapter(cmtRatings)
        rvUserComments.adapter = adapter
    }

    private fun loadComments(placeId: Int) {
        cmtRating.rating = 0F
        cmtRatingNumber.text = "0.0"
        edtCommentContent.setText("")
        loadingDiaLog.show()
        selectedUri = null
        bitmap = null

        APIUtils.mAPIServices?.getCommentRatings(placeId)
            ?.enqueue(object : Callback<UserRatingResponse> {
                override fun onFailure(call: Call<UserRatingResponse>, t: Throwable) {
                    SimpleNotify.networkError(context)
                    loadingDiaLog.dismiss()
                }

                override fun onResponse(
                    call: Call<UserRatingResponse>,
                    response: Response<UserRatingResponse>
                ) {
                    if (response.isSuccessful) {
                        val userRatingResponse = response.body()
                        if (userRatingResponse != null && userRatingResponse.code == 1) {
                            if (userRatingResponse.ratings.isEmpty()) {
                                SimpleNotify.success(
                                    context,
                                    "Hãy là người đầu tiên đánh giá địa điểm này",
                                    ""
                                )
                                loadingDiaLog.dismiss()
                                return
                            } else {
                                for (userR in userRatingResponse.ratings) {
                                    cmtRatings.add(userR)
                                    adapter.notifyDataSetChanged()
                                }
                                loadingDiaLog.dismiss()
                                return
                            }
                        }
                    }
                    SimpleNotify.error(context, "Không lấy được đánh giá của địa điểm này", "")
                    loadingDiaLog.dismiss()
                }
            })
    }

    private fun initEvents() {
        ivCloseBtn.setOnClickListener {
            dismiss()
        }
        cmtRating.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (fromUser) {
                cmtRatingNumber.text = String.format("%.01f", rating)
            }
        }
        attImg1.setOnClickListener {
            val intent = Intent(context, TempActivityForSelectFile::class.java)
            intent.putExtra("KEY", TempActivityForSelectFile.RATING_ATTR_PIC_1)
            context.startActivity(intent)
        }
    }

    class SaveTempFile(val context: Context, val placeId: Int, val rating: Float, val cmt: String) :
        AsyncTask<Void, Int, File?>() {
        override fun doInBackground(vararg params: Void?): File? {
            return SaveFileUtils.saveImages(
                context,
                "img_place.${MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(context.contentResolver.getType(selectedUri!!)!!)}",
                bitmap!!
            )
        }

        override fun onPostExecute(f: File?) {
            super.onPostExecute(f)
            if (f != null && current != null) {
                val imgReqBody = RequestBody.create(
                    MediaType.parse(context.contentResolver.getType(selectedUri!!)!!),
                    f
                )

                APIUtils.mAPIServices?.addRating(
                    RequestBody.create(
                        MediaType.parse("text/plain"), cmt
                    ), rating,
                    placeId,
                    MultipartBody.Part.createFormData("image1", f.name, imgReqBody)
                )?.enqueue(object : Callback<CheckResponse> {
                    override fun onFailure(call: Call<CheckResponse>, t: Throwable) {
                        SimpleNotify.error(context, "Lỗi khi đánh giá địa điểm", "")
                        current?.loadingDiaLog?.dismiss()
                    }

                    override fun onResponse(
                        call: Call<CheckResponse>,
                        response: Response<CheckResponse>
                    ) {
                        if (response.isSuccessful) {
                            val check = response.body()
                            if (check != null && check.code == 1) {
                                SimpleNotify.success(context, "Đánh giá thành công", "")
                                current?.loadComments(placeId)
                                return
                            }
                        }

                        SimpleNotify.error(context, "Lỗi không xác định khi đánh giá", "")
                        return
                    }
                })
            } else {
                SimpleNotify.error(context, "Lỗi khi đăng đánh giá", "")
                current?.loadingDiaLog?.dismiss()
            }
        }
    }
}