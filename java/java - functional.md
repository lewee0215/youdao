Java8 四大核心函数式接口
//T：入参类型；出参类型是Boolean
Predicate<User> predict = demo -> StringUtils.equals(demo.getName(), "cun");
		
//T：入参类型，R：出参类型
Function<User,User> nameChange = source ->{source.setName("liuwei");return source;} ;
		
//T：入参类型；没有出参
Consumer<Integer> square = (x) -> {System.out.println("print : " + x * x);};
		
//T：出参类型；没有入参
Supplier<String> sup= ()->{String name = "liu"; name = name+"wei";return name;};