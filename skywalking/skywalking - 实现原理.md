RestTemplate 拦截器 HttpHeader 解析
org.apache.skywalking.apm.agent.core.context.ContextCarrier

HttpHeader : "sw6": ["1-My41OC4xNjA1MDAyOTYxMjg1MDAwNQ==-My43MS4xNjA1MDAyOTYyOTIwMDAwMg==-1-3-3-I2xvY2FsaG9zdDo2MjYw-Iy9hZG1pbi9hc3luYw==-I1NwcmluZ0FzeW5j"],



StringUtil.join('-',

	"1",

	Base64.encode(this.getPrimaryDistributedTraceId().encode()),	//3.58.16050029612850005  ->My41OC4xNjA1MDAyOTYxMjg1MDAwNQ==

	Base64.encode(this.getTraceSegmentId().encode()),			//3.71.16050029629200002  ->My43MS4xNjA1MDAyOTYyOTIwMDAwMg==

	this.getSpanId() + "",						//1

	this.getParentServiceInstanceId() + "",			//3

	this.getEntryServiceInstanceId() + "",			//3

	Base64.encode(this.getPeerHost()),			//#localhost:6260 	->I2xvY2FsaG9zdDo2MjYw

	Base64.encode(this.getEntryEndpointName()),	//#/admin/async		->Iy9hZG1pbi9hc3luYw==

	Base64.encode(this.getParentEndpointName()));	//#SpringAsync		->I1NwcmluZ0FzeW5j



TraceId 生成规则
org.apache.skywalking.apm.agent.core.context.ids.GlobalIdGenerator
public static String generate() {

        return StringUtil.join(

            '.',

            PROCESS_ID,

            String.valueOf(Thread.currentThread().getId()),

            String.valueOf(THREAD_ID_SEQUENCE.get().nextSeq())

        );

 }