package org.orange.querysystem;

import java.util.ArrayList;

import org.orange.querysystem.R;
import org.orange.querysystem.SettingsActivity;
import org.orange.querysystem.content.dialog.TimeAndAddressSettingDialog;
import org.orange.querysystem.content.dialog.TimeAndAddressSettingDialog.TimeAndAddressSettingDialogListener;
import org.orange.querysystem.util.ParcelableTimeAndAddress;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.webpage.Course;
import util.webpage.Course.CourseException;
import util.webpage.Course.TimeAndAddress;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
//TODO 测试不同生命周期状态下的正确性
public class CourseDetailsActivity extends FragmentActivity implements TimeAndAddressSettingDialogListener{
	public static final String KEY_COURSE_CODE = CourseDetailsActivity.class.getName() + ".key_course_code";

	private static final String KEY_INSTANCE_STATE_TIME_AND_ADDRESS =
			CourseDetailsActivity.class.getName() + ".key_instance_state_time_and_address";
	private static final int NONE			= 0;
	private static final int ADD_COURSE		= 1;
	private static final int SHOW_COURSE	= 2;
	private static final int MODIFY_COURSE	= 3;
	private static final int COMMIT			= 4;
	private int mode = NONE;
	/** 正在设置的课程 */
	private Course mCourse = new Course();
	private EditText course_code_input;
	private EditText course_class_number_input;
	private EditText course_teacher_input;
	private EditText course_credit_input;
	private EditText course_kind_input;
	private EditText course_test_score_input;
	private EditText course_total_score_input;
	private EditText course_grade_point_input;
	private EditText course_name_input;
	private LinearLayout course_time_and_address_placeholder;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_course_info);
        course_name_input = (EditText)findViewById(R.id.course_name_input);
        course_code_input = (EditText)findViewById(R.id.course_code_input);
        course_class_number_input = (EditText)findViewById(R.id.course_class_number_input);
        course_teacher_input = (EditText)findViewById(R.id.course_teacher_input);
        course_credit_input = (EditText)findViewById(R.id.course_credit_input);
        course_kind_input = (EditText)findViewById(R.id.course_kind_input);
        course_test_score_input = (EditText)findViewById(R.id.course_test_score_input);
        course_total_score_input = (EditText)findViewById(R.id.course_total_score_input);
		course_grade_point_input = (EditText)findViewById(R.id.course_grade_point_input);
		course_time_and_address_placeholder = (LinearLayout) findViewById(R.id.course_time_and_address_placeholder);

		//如果有课程代码额外信息，显示此课程的详情
		if(savedInstanceState == null){
			int courseCode = getIntent().getIntExtra(KEY_COURSE_CODE, -1);
			if(courseCode != -1){
				new QueryCourseInformationFromDatabase().execute(String.valueOf(courseCode),
						SettingsActivity.getAccountStudentID(this));
			}
		}
		//用于输入新的时间地点的输入框
		addTimeAndAddressEditText();

		setMode(ADD_COURSE);
		//3.0以上版本，使用ActionBar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			//横屏时，为节省空间隐藏ActionBar
			if(getResources().getConfiguration().orientation == 
					android.content.res.Configuration.ORIENTATION_LANDSCAPE)
				getActionBar().hide();
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		//恢复时间地点设置
		ArrayList<ParcelableTimeAndAddress> saved =
				savedInstanceState.getParcelableArrayList(KEY_INSTANCE_STATE_TIME_AND_ADDRESS);
		mCourse.getTimeAndAddress().addAll(saved);
		loadCourse();
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//保存当前时间地点设置
		ArrayList<ParcelableTimeAndAddress> saving = new ArrayList<ParcelableTimeAndAddress>();
		for(TimeAndAddress aTimeAndAddress:mCourse.getTimeAndAddress())
			saving.add(new ParcelableTimeAndAddress(aTimeAndAddress));
		outState.putParcelableArrayList(KEY_INSTANCE_STATE_TIME_AND_ADDRESS, saving);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setMode(int mode){
		if(this.mode == mode) return;
		this.mode = mode;
		ActionBar mActionBar = null;
		//3.0以上版本，使用ActionBar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			mActionBar = getActionBar();
			/*On Android 3.0 and higher, the options menu is considered to always be open
			when menu items are presented in the action bar. When an event occurs and you
			want to perform a menu update, you must call invalidateOptionsMenu() to request
			that the system call onPrepareOptionsMenu().*/
			invalidateOptionsMenu();
		}
		switch(mode){
		case ADD_COURSE:
			setEditable(true);
			if(mActionBar != null) mActionBar.setTitle(R.string.add_course);
			break;
		case SHOW_COURSE:
			setEditable(false);
			if(mActionBar != null) mActionBar.setTitle(R.string.course_details);
			break;
		case MODIFY_COURSE:
			setEditable(true);
			course_code_input.setEnabled(false);
			if(mActionBar != null) mActionBar.setTitle(R.string.modify_course);
			break;
		}
	}
	private void setEditable(boolean editable){
		if(course_grade_point_input.isEnabled() == editable)
			return;
		course_name_input.setEnabled(editable);
		course_code_input.setEnabled(editable);
		course_class_number_input.setEnabled(editable);
		course_teacher_input.setEnabled(editable);
		course_credit_input.setEnabled(editable);
		course_kind_input.setEnabled(editable);
		course_test_score_input.setEnabled(editable);
		course_total_score_input.setEnabled(editable);
		course_grade_point_input.setEnabled(editable);
		for(int i=0;i<course_time_and_address_placeholder.getChildCount();i++)
			course_time_and_address_placeholder.getChildAt(i).setEnabled(editable);
		if(editable)
			addTimeAndAddressEditText();
		else
			removeTimeAndAddressEditText(false, course_time_and_address_placeholder.getChildCount()-1);
	}

	private void loadCourse(){
		loadCourse(mCourse);
	}
	private void loadCourse(Course course){
		course_name_input.setText(course.getName());
		course_code_input.setText(course.getCode());
		course_class_number_input.setText(course.getClassNumber());
		course_teacher_input.setText(course.getTeacherString());
		course_credit_input.setText(String.valueOf(course.getCredit()));
		course_kind_input.setText(course.getKind());
		//TODO 检测方法
		if(!Float.isNaN(course.getTestScore()))
			course_test_score_input.setText(String.valueOf(course.getTestScore()));
		if(!Float.isNaN(course.getTotalScore())){
			course_total_score_input.setText(String.valueOf(course.getTotalScore()));
			try {
				course_grade_point_input.setText(String.valueOf(course.getGradePoint()));
			} catch (CourseException e) {
				throw new IllegalStateException("尚未设置期末总评成绩", e);
			}
		}

		//添加时间地点
		removeTimeAndAddressEditText(false, course_time_and_address_placeholder.getChildCount()-1);
		for(TimeAndAddress timeAndAddress:course.getTimeAndAddress()){
			EditText added = addTimeAndAddressEditText();
			addTimeAndAddress(course_time_and_address_placeholder.indexOfChild(added), timeAndAddress);
		}
		addTimeAndAddressEditText();
	}

	/**
	 * 把时间地点{@link EditText}列表的第{@code index}个更新，并设置{@link TimeAndAddress}列表的第{@code index}个
	 * @param index 索引
	 * @param aTimeAndAddress 新（被更新的）时间地点
	 */
	private void addTimeAndAddress(int index, TimeAndAddress aTimeAndAddress){
		if(index < 0)
			throw new IllegalArgumentException("非法索引：" + index);
		((EditText)course_time_and_address_placeholder.getChildAt(index)).setText(aTimeAndAddress.toString());
		//如果这是新时间地点，应该有index==mCourse.getTimeAndAddress().size()
		if(index < mCourse.getTimeAndAddress().size()){
			mCourse.getTimeAndAddress().set(index, new TimeAndAddress(aTimeAndAddress));
		}else if(index == mCourse.getTimeAndAddress().size()){	//新课程
			mCourse.getTimeAndAddress().add(new TimeAndAddress(aTimeAndAddress));
			addTimeAndAddressEditText();
		}else
			throw new IllegalArgumentException("非法索引：" + index);
	}

	private void removeTimeAndAddressEditText(boolean withData, int index){
		course_time_and_address_placeholder.removeViewAt(index);
		if(withData)
			mCourse.getTimeAndAddress().remove(index);
	}
	/**
	 * 新增一个时间地点输入框
	 * @return 新增的时间地点{@link EditText}
	 */
	private EditText addTimeAndAddressEditText(){
		EditText editText = new EditText(this);
		editText.setInputType(InputType.TYPE_NULL);
		editText.setCursorVisible(false);
		editText.setLongClickable(false);
		editText.setFocusable(false);
		editText.setOnClickListener(mOnClickTimeAndAddressEditTextListener);
		course_time_and_address_placeholder.addView(editText,
				new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		return editText;
	}

	private final OnClickListener mOnClickTimeAndAddressEditTextListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int index = course_time_and_address_placeholder.indexOfChild(v);
			TimeAndAddress initialValue = null;
			//如果这是新时间地点，应该有index==mCourse.getTimeAndAddress().size()
			if(index != mCourse.getTimeAndAddress().size())
				initialValue = mCourse.getTimeAndAddress().get(index);
			showTimeAndAddressSettingDialog(initialValue, String.valueOf(index));
		}
	};
	private void showTimeAndAddressSettingDialog(TimeAndAddress initialTimeAndAddress, String tag){
		DialogFragment mTimeAndAddressSettingsFragment = TimeAndAddressSettingDialog.newInstance(initialTimeAndAddress, tag);
		mTimeAndAddressSettingsFragment.show(getSupportFragmentManager(), "fragment_dialog");
	}
	@Override
	public void onDialogPositiveClick(TimeAndAddressSettingDialog dialog, String tag, TimeAndAddress aTimeAndAddress) {
		addTimeAndAddress(Integer.valueOf(tag), aTimeAndAddress);
	}
	@Override
	public void onDialogNegativeClick(TimeAndAddressSettingDialog dialog, String tag) {}

	private void saveCoursesInDatabase(){
		mCourse.setCode(course_code_input.getText().toString());
		mCourse.setName(course_name_input.getText().toString());
		mCourse.setClassNumber(course_class_number_input.getText().toString());
		mCourse.setTeachers(course_teacher_input.getText().toString());
		mCourse.setKind(course_kind_input.getText().toString());
		try {
			mCourse.setCredit(Integer.parseInt(course_credit_input.getText().toString()));
		} catch (Exception e) {
			course_credit_input.requestFocus();
			//TODO 提示
		}
		try {
			mCourse.setTestScore(Float.parseFloat(course_test_score_input.getText().toString()));
		} catch (Exception e1) {
			course_test_score_input.requestFocus();
			//TODO 提示
		}
		try {
			mCourse.setTotalScore(Float.parseFloat(course_total_score_input.getText().toString()));
		} catch (Exception e) {
			course_total_score_input.requestFocus();
			//TODO 提示
		}
        String userName = SettingsActivity.getAccountStudentID(this);
		new SaveCourseInDatabase().execute(mode, mCourse, userName);
    }

	/**
	 * 把课程信息存储到数据库。用execute(Integer mode, Course course, String userName)启动异步线程
	 */
	private class SaveCourseInDatabase extends AsyncTask<Object,Void,Void>{
		@Override
		protected Void doInBackground(Object... args) {
			int mode = (Integer) args[0];
			StudentInfDBAdapter studentInfDBAdapter = new StudentInfDBAdapter(CourseDetailsActivity.this);
			try {
				studentInfDBAdapter.open();
				switch(mode){
				case ADD_COURSE:
					studentInfDBAdapter.autoInsertCourseInf((Course)args[1], (String)args[2]);
					//TODO 失败提示及处理
					//此处调用的方法返回布尔值，当为true是表示成功插入了新增课程，且能显示在本学期课程项中，当为false时表示插入不成功，用户输入的课程代码在数据库中已经有了。要给用户一个提示。
					studentInfDBAdapter.updateCurrentSemesterOfAddCourseInf((Course)args[1]);
					break;
				case MODIFY_COURSE:
					studentInfDBAdapter.updateCourseInf((Course)args[1]);
					studentInfDBAdapter.updateScoreInf((Course)args[1]);
					break;
				default:throw new IllegalArgumentException("非法参数：mode = " + mode);
				}
			} catch(SQLiteException e){
				//TODO 异常处理
				e.printStackTrace();
			} finally {
				studentInfDBAdapter.close();
			}
			return null;
		}
	}

	private class QueryCourseInformationFromDatabase extends AsyncTask<String, Void, Course>{
		@Override
		protected Course doInBackground(String... params) {
			Course result = null;
			StudentInfDBAdapter studentInfDBAdapter = new StudentInfDBAdapter(CourseDetailsActivity.this);
			try{
				studentInfDBAdapter.open();
				result = studentInfDBAdapter.getCourseFromDB(StudentInfDBAdapter.KEY_ID + "=" + params[0], params[1]);
			} catch (SQLiteException e){
				//TODO 异常处理
				e.printStackTrace();
			} finally {
				studentInfDBAdapter.close();
			}
			return result;
		}
		@Override
		protected void onPostExecute(Course result) {
			if(result != null){
				mCourse = result;
				loadCourse();
				setMode(SHOW_COURSE);
			}
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, MODIFY_COURSE, Menu.NONE, R.string.course_info_change);
		menu.add(Menu.NONE, COMMIT, Menu.NONE, R.string.course_info_submit);
		return true;
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuItem modify = menu.findItem(MODIFY_COURSE);
		MenuItem commit = menu.findItem(COMMIT);
		switch(mode){
		case ADD_COURSE:case MODIFY_COURSE:
			modify.setVisible(false);
			modify.setEnabled(false);
			commit.setVisible(true);
			commit.setEnabled(true);
			break;
		case SHOW_COURSE:
			modify.setVisible(true);
			modify.setEnabled(true);
			commit.setVisible(false);
			commit.setEnabled(false);
			break;
		}
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case COMMIT:
			saveCoursesInDatabase();
			finish();
			return true;
		case MODIFY_COURSE:
			setMode(MODIFY_COURSE);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}