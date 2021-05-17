# chat
### spring + redis + mysql chatting program
### [redis 번역본](http://arahansa.github.io/docs_spring/redis.html)
### [redis 자료구조](https://sabarada.tistory.com/105)
### [redis HashOperation 사용하기](https://stackabuse.com/spring-boot-with-redis-hashoperations-crud-functionality/)

### 1. skill
```java
1. web
2. redis
3. mysql
4. webSocket
5. Lombok
```

### 2. redis 설치 완료

### 3. RedisConfig 설정 
```java
@Configuration
public class RedisConfig {

    /**RedisConnectionFactory를 통해 내장 혹은 외부의 Redis를 연결합니다.**/
    // RedisConnection들은 RedisConnectionFactory을 통해서 생성

    /**
     * redis listener 설정
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListener(RedisConnectionFactory connectionFactory){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    /**
     * Redis를 직접적으로 사용하는 RedisTemplate 설정
     *
     * RedisTemplate을 통해 RedisConnection에서 넘겨준 byte 값을 객체 직렬화합니다.
     *
     * RedisTemplate은 스레드세이프하며 여러개의 인스턴스에서 재사용될수 있습니다
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        /** key, value에 대해 Serializer하게 설정 **/
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
        return redisTemplate;
    }
}
```

### 4. WebSocketConfig - STOMP 관련 설정
```java
* WebSocketMessageBrokerConfigurer을 구현 한다

*Websocket api를 바로 이용하지 않고 STOMP를 통해서 설정을 할 것이다.
 - configureMessageBroker : 메시지 브로커에 관련된 설정을 한다
 - registerStompEndpoints : SockJs Fallback을 이용해 노출할 STOMP endpoint를 설정한다.


@Configuration
@EnableWebSocketMessageBroker
public class ChatWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * configureMessageBroker에서 Application 내부에서 사용할 path를 지정 할 수 있다.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        /** setApplicationDestinationPrefixes : client에서 SEND 요청을 처리한다. **/
        /*
        Spring Reference에서는 /topic, /queue가 주로 등장하는데 여기서는 이해를 돕기 위해 /pub 으로 지정하였다.
         /topic : 암시적으로 1:N 전파를 의미한다.
         /queue : 암시적으로 1:1 전파를 의미한다.
         */
        registry.setApplicationDestinationPrefixes("/pub");

        /** 해당 경로로 SimpleBroker를 등록한다.
         * SimpleBroker는 해당하는 경로를 SUBSCRIBE하는 client에게
         * 메시지를 전달하는 간단한 작업을 수행한다 **/
        registry.enableSimpleBroker("/sub");


        /*
        + enableStompBrokerRelay : SimpleBroker의 기능과
        외부 message broker(RabbitMQ, ActiveMQ 등)에 메시지를 전달하는 기능을 가지고 있다.
         */
    }


    /**
     *  handshake와 통신을 담당할 endpoint를 지정
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat").setAllowedOrigins("*").withSockJS();
    }
}
```
### .[Stomp 모델](https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte3.9:ptl:stomp)
```
SEND
destination:/queue/trade
content-type:application/json
content-length:44
 
{"action":"BUY","ticker":"MMM","shares",44}^@
```

### .configureMessageBroker와 STOMP 메시지 설명
1. #### 클라이언트가 SEND 프레임을 다음과 같이 보낼 때
```
>>> SEND
destination:/pub/message
content-length:83

{"roomId":"1","userId":"6666","message":"123123","date":"2019-02-11T15:04:59.958Z"}
```
2. #### @Controller에서는 /pub desination prefix를 제외한 경로 /message를 @MessageMapping하면 된다.
```java
@RequiredArgsConstructor
@Controller
public class ChatController {

    /**
     * @MessageMapping("url") : "url"으로 들어오는 메시지 매핑할때 사용하는 애노테이션
     * "/pub/chat/message" 으로 들어오는 message를 ChatMessage으로 바인딩하여 실행
     */
    @MessageMapping("/chat/message")
    public void message(ChatMessage message) {
        //...
    }
}
```


### 5. Message Model (ChatRoom, ChatMessage)
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoom implements Serializable {
    
    private Long id;

    private String name;

    private Long manId;

    private Long womanId;
}

@Data
public class ChatMessage {
    // 메시지 타입 : 입장, 채팅
    public enum MessageType {
        ENTER, TALK, LEAVE
    }
    private MessageType type; // 메시지 타입
    private String roomId; // 방번호
    private String sender; // 메시지 보낸사람
    private String message; // 메시지
}
```
### 6. PUB 생성 - @Service
```java
@Service
public class PublisherService {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 해당 메소드는 채널 topic과 메시지를 받아서
     */
    public void publish(ChannelTopic topic, ChatMessage message){
        /** 채널 topic에 메시지 보내기 **/
       redisTemplate.convertAndSend(topic.getTopic(), message);
    }
}
```
### 7. SUB 생성 - @Service
```java
@Service
public class PublisherService {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 해당 메소드는 채널 topic과 메시지를 받아서
     */
    public void publish(ChannelTopic topic, ChatMessage message){
        /** 채널 topic에 메시지 보내기 **/
       redisTemplate.convertAndSend(topic.getTopic(), message);
    }
}
```
### cf. hashOperation<String,String,ChatRoom> 사용하기
```java

    private static final String CHAT_ROOMS = "CHAT_ROOM";
    
    /** (1) 값넣기
        * CHAT_ROOM 을 key로하는 hashmap에
        * chatRoom 객체의 room id를 key로
        * chatRoom 객체를 value로 저장한다 **/
    opsHashChatRoom.put(CHAT_ROOMS,chatRoom.getRoomId(),chatRoom);

    /** (2) 값 가져오기
        * CHAT_ROOM 을 key로하는 hashmap에
        * roomId를 key로 하는 value(chatRoom 객체)를 받아온다.
        */
    ChatRoom chatRoom = opsHashChatRoom.get(CHAT_ROOMS, roomId);

    /** (3) hashmap을 다루는 key에 해당하는 key-value값 모두 가져오기
        * CHAT_ROOM 을 key로하는 hashmap을 그대로 가져온다.
        */
    Map<String,ChatRoom> chatRooms = opsHashChatRoom.entries(CHAT_ROOMS);


    /** (4) hashmap을 다루는 key에 해당하는 value값 모두 가져오기
        * CHAT_ROOM 을 key로하는 hashmap에
        * 모든 value(chatRoom 객체)를 받아온다.
        */
    List<ChatRoom> chatRooms = opsHashChatRoom.values(CHAT_ROOMS);

    /** (5) 삭제하기
        * CHAT_ROOM 을 key로하는 hashmap에
        * roomId가 key인 entry 삭제
        */
    opsHashChatRoom.delete(CHAT_ROOMS,roomId);

```

### 8. Repository 추가 - redis put,get // charRoom
```java
@Repository
@RequiredArgsConstructor
public class ChatRepository {

    /** 채팅방(Topic)에 발행되는 메시지를 처리할 Listner **/
    private final RedisMessageListenerContainer redisMessageListener;

    /** Subscriver 전용 서비스**/
    private final SubscriberService subscriberService;

    /** Redis 로직 처리 **/
    private static final String CHAT_ROOMS = "CHAT_ROOM";
    private final RedisTemplate redisTemplate;

    /**
     * Data Access
     * 사용법은 init()에서 참고
     */
    private HashOperations<String,String, ChatRoom> opsHashChatRoom;


    /**
     * 채팅방의 대화 메시지를 발행하기 위한 redis topic 정보.
     * 서버별로 채팅방에 매치되는 topic정보를 Map에 넣어 roomId로 찾을수 있도록 한다.
     */
    private Map<String, ChannelTopic> topics = new HashMap<>();

    /** @PostConstruct는 의존성 주입이 이루어진 후 초기화를 수행하는 메서드이다 **/
    @PostConstruct
    private void init(){
        /** 1. redisTemplate에서 operation 받기 **/
        opsHashChatRoom = redisTemplate.opsForHash();
    }
    public List<ChatRoom> findAllRoom() {
        return opsHashChatRoom.values(CHAT_ROOMS);
    }

    public ChatRoom findRoomById(String id) {
        return opsHashChatRoom.get(CHAT_ROOMS, id);
    }

    /**=========================채팅방 생성, 찾기**/

    /**
     * 1. 채팅방 만들고
     * 2. 해당 채팅방에 통신을 담당할 리스너 설정
     */
    public void enterChatRoom(String roomId){
        /** 1. 동일한 채팅방 있는지 검사 **/
        ChannelTopic topic = topics.get(roomId);
        if(topic == null){
            /** 2. 없으면 새로운 채팅방 만들어서 저장 **/
            topic = new ChannelTopic(roomId);
            topics.put(roomId,topic);
        }
        /** 3. 해당 채팅방(topic)의 통신을 담당 할 리스너(subscriberService) 등록 **/
        redisMessageListener.addMessageListener(subscriberService, topic);
    }

    /**
     * 채팅 방 목록(topics)에서 roomId에 해당하는 채팅방 이름 넘기기
     */
    public ChannelTopic getTopic(String roomId){
        return topics.get(roomId);
    }
}
```
### 9. Message 처리하는 Controller
```java
* message를 받아서 채팅방 없으면 만들고 있으면 해당하는 채팅방에 메세지를 보낸다.
@RequiredArgsConstructor
@Controller
public class ChatController {

    private final PublisherService redisPublisher;
    private final ChatRepository chatRepository;

    /**
     * @MessageMapping("url") : "url"으로 들어오는 메시지 매핑할때 사용하는 애노테이션
     * "/pub/chat/message" 으로 들어오는 message를 ChatMessage으로 바인딩하여 실행
     */
    @MessageMapping("/chat/message")
    public void message(ChatMessage message) {
        /** Message Type이 Enter이면 입장 **/
        if (ChatMessage.MessageType.ENTER.equals(message.getType())) {
            /** 1. 방 번호로 채팅방을 생성 **/
            chatRepository.enterChatRoom(message.getRoomId());
            /** 2. 객체에 입장한다는 메세지 담기 **/
            message.setMessage(message.getSender() + "님이 입장하셨습니다.");
        }

        /** 1. 생성된 방 목록에서 roomId에 해당하는 방 가져와서 **/
        ChannelTopic topic = chatRepository.getTopic(message.getRoomId());
        /** 2. 해당 방에 message 전달하기 **/
        redisPublisher.publish(topic, message);
    }
}
```

### 10. 채팅방 입장,생성 처리하는 Controller
```java
@RequiredArgsConstructor
@Controller
@RequestMapping("/chat")
public class ChatRoomController {

    private final ChatRepository chatRoomRepository;

    /**
     * main Page 보여주기
     */
    @GetMapping("/room")
    public String rooms(Model model) {
        return "/chat/room";
    }

    /**
     * 현재 있는 모든 채팅방 반환
     */
    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoom> room() {
        return chatRoomRepository.findAllRoom();
    }

    /**
     * 채팅 룸 생성
     */
    @PostMapping("/room")
    @ResponseBody
    public ChatRoom createRoom(@RequestParam String name) {
        return chatRoomRepository.createChatRoom(name);
    }

    /**
     * 채팅방 Page 리턴
     */
    @GetMapping("/room/enter/{roomId}")
    public String roomDetail(Model model, @PathVariable String roomId) {
        model.addAttribute("roomId", roomId);
        return "/chat/roomdetail";
    }

    /**
     * 채팅방 번호로 찾기
     */
    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ChatRoom roomInfo(@PathVariable String roomId) {
        return chatRoomRepository.findRoomById(roomId);
    }
}
```