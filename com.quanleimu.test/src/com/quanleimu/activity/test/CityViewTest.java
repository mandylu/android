import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.Test;


public class CityViewTest extends BaixingTestCase {
	private static final String LOG_TAG = "MainActivityTest";
	
	public CityViewTest() throws Exception {
		super();
		AthrunTestCase.setMaxTimeToFindView(10000);
	}
	
	@Test
	
	public void testCityView() throws Exception {
		
		//home页点击百姓网
		//检查title文字为“选择城市”
		//点击深圳
		//检查home title为“百姓网 深圳”
		//再次在home页点击百姓网
		//滑动当前页至下方，点击“选择其他城市”
		//检查title为“选择省份”
		//点击返回
		//检查title为“选择城市”
		//再次滑动当前页至下方，点击“选择其他城市”
		//点击“浙江”
		//检查title为“选择城市”
		//点击“杭州”
		//检查home title为“百姓网 杭州”
		
	}
}