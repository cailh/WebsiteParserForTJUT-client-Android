/**
 *
 */
package org.orange.querysystem.content;

import java.util.ArrayList;
import java.util.List;

import org.orange.querysystem.R;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @author Bai Jie
 *
 */
public class ListScoresFragment extends ListFragment {
	public static final String SCORES_KEY = ListScoresFragment.class.getName()+"simplescores_key";
	private View mHeaderView;
	private ScoresAdapter mScoresAdapter;

	public static ListScoresFragment newInstance(ArrayList<SimpleScore> scores){
		ListScoresFragment scoresFragment = new ListScoresFragment();
		Bundle args = new Bundle();
		args.putParcelableArrayList(SCORES_KEY, scores);
		scoresFragment.setArguments(args);
		return scoresFragment;
	}

	/**
	 * 应用{@link #setArguments(Bundle args)}设置的Scores，{@link #onCreate(Bundle)}会自动调用此方法<br />
	 * 使用方法：<br />
	 * 先用{@link #setArguments(Bundle args)}方法传递成绩列表，再用此方法应用传递过来的列表
	 * <code>
	 * ArrayList<SimpleScore> scores;
	 * ...
	 * Bundle arg = new Bundle();
	 * arg.putParcelableArrayList(ListScoresFragment.SCORES_KEY, scores);
	 * thisFragment.setArguments(arg);
	 * thisFragment.applyArguments();
	 * </code>
	 */
	public void applyArguments(){
		Bundle args = getArguments();
		if(args != null){
			ArrayList<SimpleScore> scores = args.getParcelableArrayList(SCORES_KEY);
			mScoresAdapter = new ScoresAdapter(getActivity(), scores);
			setListAdapter(mScoresAdapter);
		}
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		applyArguments();
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//生成Header
		mHeaderView = inflater.inflate(R.layout.fragment_list_score_row, null);
		((TextView)mHeaderView.findViewById(R.id.course_name)).setText(R.string.course_name);
		((TextView)mHeaderView.findViewById(R.id.course_test_score)).setText(R.string.course_test_score);
		((TextView)mHeaderView.findViewById(R.id.course_total_score)).setText(R.string.course_total_score);
		((TextView)mHeaderView.findViewById(R.id.course_grade_point)).setText(R.string.course_grade_point);
		((TextView)mHeaderView.findViewById(R.id.course_credit)).setText(R.string.course_credit);
		((TextView)mHeaderView.findViewById(R.id.course_kind)).setText(R.string.course_kind);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		//添加Header
		setListAdapter(null);
		getListView().addHeaderView(mHeaderView);
		setListAdapter(mScoresAdapter);
		super.onActivityCreated(savedInstanceState);
	}

	public static class SimpleScore implements Parcelable{
		private int id;
		/**课程名称*/
		private String name;
		/**结课考核成绩*/
		private short testScore;
		/**期末总评成绩*/
		private short totalScore;
		/**绩点*/
		private float gradePoint;
		/**学分*/
		private byte credit;
		/**课程性质*/
		private String kind;

		/**
		 * @param id　课程ID
		 * @param name 课程名称
		 * @param testScore 结课考核成绩
		 * @param totalScore 总评成绩
		 * @param gradePoint 绩点
		 * @param credit 学分
		 * @param kind 课程性质
		 */
		public SimpleScore(int id, String name, short testScore,
				short totalScore, float gradePoint, byte credit, String kind) {
			super();
			this.id = id;
			this.name = name;
			this.testScore = testScore;
			this.totalScore = totalScore;
			this.gradePoint = gradePoint;
			this.credit = credit;
			this.kind = kind;
		}
		private SimpleScore(Parcel in){
			this(in.readInt(),in.readString(),(short)in.readInt(),(short)in.readInt(),
					in.readFloat(),in.readByte(),in.readString());
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getTestScore() {
			return String.valueOf(testScore);
		}

		public String getTotalScore() {
			return String.valueOf(totalScore);
		}

		public String getGradePoint() {
			return String.valueOf(gradePoint);
		}

		public String getCredit() {
			return String.valueOf(credit);
		}

		public String getKind() {
			return kind;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(id);
			dest.writeString(name);
			dest.writeInt(totalScore);
			dest.writeInt(totalScore);
			dest.writeFloat(gradePoint);
			dest.writeByte(credit);
			dest.writeString(kind);
		}
		public static final Parcelable.Creator<SimpleScore> CREATOR
		= new Parcelable.Creator<SimpleScore>(){

			@Override
			public SimpleScore createFromParcel(Parcel source) {
				return new SimpleScore(source);
			}

			@Override
			public SimpleScore[] newArray(int size) {
				return new SimpleScore[size];
			}

		};
	}

	public static class ScoresAdapter extends BaseAdapter{
		private LayoutInflater mInflater;
		private List<SimpleScore> scores;
		public ScoresAdapter(Context context, List<SimpleScore> scores){
			mInflater = LayoutInflater.from(context);
			setScores(scores);
		}
		public void setScores(List<SimpleScore> scores){
			this.scores = scores;
		}

		@Override
		public int getCount() {
			if(scores!=null)
				return scores.size();
			return 0;
		}

		@Override
		public SimpleScore getItem(int position) {
			return scores.get(position);
		}

		@Override
		public long getItemId(int position) {
			return scores.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// A ViewHolder keeps references to children views to avoid unneccessary calls
			// to findViewById() on each row.
			ViewHolder holder;

			// When convertView is not null, we can reuse it directly, there is no need
			// to reinflate it. We only inflate a new View when the convertView supplied
			// by ListView is null.
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.fragment_list_score_row,parent,false);

				// Creates a ViewHolder and store references to the two children views
				// we want to bind data to.
				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.course_name);
				holder.testScore = (TextView) convertView.findViewById(R.id.course_test_score);
				holder.totalScore = (TextView) convertView.findViewById(R.id.course_total_score);
				holder.gradePoint = (TextView) convertView.findViewById(R.id.course_grade_point);
				holder.credit = (TextView) convertView.findViewById(R.id.course_credit);
				holder.kind = (TextView) convertView.findViewById(R.id.course_kind);

				convertView.setTag(holder);
			} else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data efficiently with the holder.
			holder.name.setText(scores.get(position).getName());
			holder.testScore.setText(scores.get(position).getTestScore());
			holder.totalScore.setText(scores.get(position).getTotalScore());
			holder.gradePoint.setText(scores.get(position).getGradePoint());
			holder.credit.setText(scores.get(position).getCredit());
			holder.kind.setText(scores.get(position).getKind());

			return convertView;
		}

		private static class ViewHolder{
			TextView name;
			TextView testScore;
			TextView totalScore;
			TextView gradePoint;
			TextView credit;
			TextView kind;
		}
	}
}