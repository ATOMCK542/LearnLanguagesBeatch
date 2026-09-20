"""High-frequency trilingual lemmas for extra packs. Format: en|ru|vi per line."""
from __future__ import annotations

# filename, theme_id, order, title{en,ru,vi}, prefix, block


def _lines(block: str) -> list[tuple[str, str, str]]:
    rows = []
    for raw in block.strip().splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        en, ru, vi = [p.strip() for p in line.split("|", 2)]
        rows.append((en, ru, vi))
    return rows


def extra_rows(prefix: str, block: str) -> list[tuple[str, str, str, str, str]]:
    rows = []
    for en, ru, vi in _lines(block):
        slug = "".join(ch if ch.isalnum() else "_" for ch in en.lower()).strip("_")
        cid = f"{prefix}.{slug}"
        word_kind = "phrase" if " " in en else "word"
        rows.append((cid, word_kind, en, ru, vi))
    return rows


FAMILY = """
mother|мама|mẹ
father|папа|bố
parent|родитель|cha mẹ
parents|родители|bố mẹ
child|ребёнок|trẻ em
children|дети|con cái
son|сын|con trai
daughter|дочь|con gái
brother|брат|anh / em trai
sister|сестра|chị / em gái
baby|малыш|em bé
husband|муж|chồng
wife|жена|vợ
grandmother|бабушка|bà
grandfather|дедушка|ông
grandson|внук|cháu trai
granddaughter|внучка|cháu gái
uncle|дядя|chú / bác
aunt|тётя|cô / dì
cousin|двоюродный брат / сестра|anh chị em họ
family|семья|gia đình
friend|друг|bạn
boyfriend|парень|bạn trai
girlfriend|девушка|bạn gái
neighbor|сосед|hàng xóm
guest|гость|khách
people|люди|người
man|мужчина|đàn ông
woman|женщина|phụ nữ
boy|мальчик|cậu bé
girl|девочка|cô bé
person|человек|người
adult|взрослый|người lớn
kid|ребёнок|trẻ
Mr|господин|ông
Mrs|госпожа|bà
Ms|госпожа|cô
couple|пара|cặp đôi
relative|родственник|họ hàng
"""

HOME = """
house|дом|nhà
apartment|квартира|căn hộ
room|комната|phòng
bedroom|спальня|phòng ngủ
bathroom|ванная|phòng tắm
kitchen|кухня|nhà bếp
living room|гостиная|phòng khách
door|дверь|cửa
window|окно|cửa sổ
wall|стена|tường
floor|пол|sàn
roof|крыша|mái
ceiling|потолок|trần
table|стол|bàn
chair|стул|ghế
bed|кровать|giường
sofa|диван|ghế sofa
lamp|лампа|đèn
light|свет|ánh sáng
key|ключ|chìa khóa
lock|замок|ổ khóa
phone|телефон|điện thoại
computer|компьютер|máy tính
television|телевизор|tivi
fridge|холодильник|tủ lạnh
stove|плита|bếp
oven|духовка|lò nướng
microwave|микроволновка|lò vi sóng
plate|тарелка|đĩa
bowl|миска|bát
fork|вилка|nĩa
knife|нож|dao
spoon|ложка|thìa
bottle|бутылка|chai
bag|сумка|túi
box|коробка|hộp
paper|бумага|giấy
pen|ручка|bút
pencil|карандаш|bút chì
notebook|тетрадь|vở
clock|часы|đồng hồ
mirror|зеркало|gương
pillow|подушка|gối
blanket|одеяло|chăn
towel|полотенце|khăn
soap|мыло|xà phòng
shower|душ|vòi sen
sink|раковина|bồn rửa
garden|сад|vườn
yard|двор|sân
balcony|балкон|ban công
stairs|лестница|cầu thang
elevator|лифт|thang máy
shelf|полка|kệ
drawer|ящик|ngăn kéo
closet|шкаф|tủ
curtain|штора|rèm
carpet|ковёр|thảm
fan|вентилятор|quạt
heater|обогреватель|máy sưởi
wifi|вайфай|wifi
electricity|электричество|điện
garbage|мусор|rác
address|адрес|địa chỉ
upstairs|наверху|tầng trên
downstairs|внизу|tầng dưới
furniture|мебель|nội thất
desk|письменный стол|bàn học
armchair|кресло|ghế bành
"""

BODY = """
head|голова|đầu
hair|волосы|tóc
face|лицо|mặt
eye|глаз|mắt
ear|ухо|tai
nose|нос|mũi
mouth|рот|miệng
tooth|зуб|răng
teeth|зубы|răng
tongue|язык|lưỡi
neck|шея|cổ
shoulder|плечо|vai
arm|рука|cánh tay
hand|кисть|tay
finger|палец|ngón tay
chest|грудь|ngực
back|спина|lưng
stomach|живот|bụng
leg|нога|chân
knee|колено|đầu gối
foot|ступня|bàn chân
feet|ступни|bàn chân
skin|кожа|da
blood|кровь|máu
heart|сердце|tim
bone|кость|xương
brain|мозг|não
body|тело|cơ thể
voice|голос|giọng
smile|улыбка|nụ cười
beard|борода|râu
lip|губа|môi
cheek|щека|má
forehead|лоб|trán
throat|горло|cổ họng
waist|талия|eo
hip|бедро|hông
ankle|лодыжка|mắt cá
wrist|запястье|cổ tay
nail|ноготь|móng
muscle|мышца|cơ
breath|дыхание|hơi thở
shoulder blade|лопатка|xương bả vai
elbow|локоть|khuỷu tay
thumb|большой палец|ngón cái
"""

CLOTHES = """
clothes|одежда|quần áo
shirt|рубашка|áo sơ mi
t-shirt|футболка|áo thun
pants|брюки|quần
jeans|джинсы|quần jean
dress|платье|váy
skirt|юбка|váy ngắn
jacket|куртка|áo khoác
coat|пальто|áo choàng
sweater|свитер|áo len
hat|шляпа|mũ
cap|кепка|mũ lưỡi trai
shoes|обувь|giày
boots|ботинки|giày boot
socks|носки|tất
underwear|белье|đồ lót
pajamas|пижама|đồ ngủ
belt|ремень|thắt lưng
tie|галстук|cà vạt
scarf|шарф|khăn quàng
gloves|перчатки|găng tay
glasses|очки|kính
watch|наручные часы|đồng hồ đeo tay
ring|кольцо|nhẫn
necklace|ожерелье|dây chuyền
wallet|кошелёк|ví
umbrella|зонт|ô
pocket|карман|túi quần
button|пуговица|cúc
zipper|молния|khóa kéo
size|размер|cỡ
fashion|мода|thời trang
uniform|форма|đồng phục
swimsuit|купальник|đồ bơi
raincoat|дождевик|áo mưa
slippers|тапочки|dép lê
sandals|сандалии|dép xăng đan
"""

COLORS = """
red|красный|đỏ
blue|синий|xanh dương
green|зелёный|xanh lá
yellow|жёлтый|vàng
black|чёрный|đen
white|белый|trắng
orange|оранжевый|cam
pink|розовый|hồng
purple|фиолетовый|tím
brown|коричневый|nâu
gray|серый|xám
gold|золотой|vàng kim
silver|серебряный|bạc
dark|тёмный|tối
bright|яркий|sáng
light color|светлый|màu nhạt
color|цвет|màu
"""

WEATHER = """
weather|погода|thời tiết
sun|солнце|mặt trời
sunny|солнечно|nắng
rain|дождь|mưa
rainy|дождливо|mưa
cloud|облако|mây
cloudy|облачно|nhiều mây
wind|ветер|gió
windy|ветрено|gió
snow|снег|tuyết
snowy|снежно|có tuyết
storm|шторм|bão
thunder|гром|sấm
lightning|молния|chớp
fog|туман|sương mù
hot weather|жара|trời nóng
cold weather|холод|trời lạnh
warm weather|тепло|trời ấm
cool weather|прохладно|trời mát
temperature|температура|nhiệt độ
degree|градус|độ
umbrella|зонт|ô
season|сезон|mùa
spring|весна|mùa xuân
summer|лето|mùa hè
autumn|осень|mùa thu
winter|зима|mùa đông
sky|небо|bầu trời
star|звезда|ngôi sao
moon|луна|mặt trăng
"""

PLACES = """
city|город|thành phố
town|городок|thị trấn
village|деревня|làng
country|страна|quốc gia
street|улица|đường
road|дорога|con đường
square|площадь|quảng trường
park|парк|công viên
bridge|мост|cầu
river|река|sông
sea|море|biển
beach|пляж|bãi biển
mountain|гора|núi
forest|лес|rừng
lake|озеро|hồ
island|остров|đảo
building|здание|tòa nhà
shop|магазин|cửa hàng
supermarket|супермаркет|siêu thị
market|рынок|chợ
mall|торговый центр|trung tâm thương mại
restaurant|ресторан|nhà hàng
cafe|кафе|quán cà phê
hotel|отель|khách sạn
hospital|больница|bệnh viện
school|школа|trường học
university|университет|đại học
library|библиотека|thư viện
bank|банк|ngân hàng
post office|почта|bưu điện
police|полиция|cảnh sát
station|станция|nhà ga
airport|аэропорт|sân bay
port|порт|cảng
museum|музей|bảo tàng
cinema|кинотеатр|rạp chiếu phim
theater|театр|nhà hát
church|церковь|nhà thờ
temple|храм|đền / chùa
office|офис|văn phòng
factory|завод|nhà máy
farm|ферма|nông trại
zoo|зоопарк|sở thú
gym|спортзал|phòng gym
pharmacy|аптека|nhà thuốc
"""

ANIMALS = """
animal|животное|động vật
dog|собака|chó
cat|кошка|mèo
bird|птица|chim
fish|рыба|cá
horse|лошадь|ngựa
cow|корова|bò
pig|свинья|heo
sheep|овца|cừu
chicken|курица|gà
duck|утка|vịt
mouse|мышь|chuột
rabbit|кролик|thỏ
bear|медведь|gấu
lion|лев|sư tử
tiger|тигр|hổ
elephant|слон|voi
monkey|обезьяна|khỉ
snake|змея|rắn
frog|лягушка|ếch
insect|насекомое|côn trùng
bee|пчела|ong
butterfly|бабочка|bướm
pet|питомец|thú cưng
wild|дикий|hoang dã
"""

NATURE = """
tree|дерево|cây
flower|цветок|hoa
grass|трава|cỏ
leaf|лист|lá
plant|растение|cây trồng
earth|земля|trái đất
ground|почва|đất
stone|камень|đá
sand|песок|cát
fire|огонь|lửa
air|воздух|không khí
nature|природа|thiên nhiên
world|мир|thế giới
field|поле|cánh đồng
hill|холм|đồi
valley|долина|thung lũng
desert|пустыня|sa mạc
ocean|океан|đại dương
wave|волна|sóng
"""

FOOD_MORE = """
apple|яблоко|táo
banana|банан|chuối
orange fruit|апельсин|cam
lemon|лимон|chanh
grape|виноград|nho
mango|манго|xoài
watermelon|арбуз|dưa hấu
strawberry|клубника|dâu tây
tomato|помидор|cà chua
potato|картофель|khoai tây
carrot|морковь|cà rốt
onion|лук|hành
garlic|чеснок|tỏi
pepper|перец|ớt / tiêu
salt|соль|muối
oil|масло|dầu
egg|яйцо|trứng
cheese|сыр|phô mai
butter|сливочное масло|bơ
yogurt|йогурт|sữa chua
soup|суп|súp
salad|салат|salad
noodle|лапша|mì
pasta|паста|mì Ý
pizza|пицца|pizza
sandwich|бутерброд|bánh sandwich
cake|торт|bánh ngọt
cookie|печенье|bánh quy
ice cream|мороженое|kem
chocolate|шоколад|sô cô la
candy|конфета|kẹo
juice|сок|nước ép
beer|пиво|bia
wine|вино|rượu vang
breakfast|завтрак|bữa sáng
lunch|обед|bữa trưa
dinner|ужин|bữa tối
snack|закуска|đồ ăn vặt
hungry|голодный|đói
thirsty|хочет пить|khát
delicious|вкусный|ngon
spicy|острый|cay
sweet|сладкий|ngọt
sour|кислый|chua
bitter|горький|đắng
salty|солёный|mặn
fresh|свежий|tươi
raw|сырой|sống
cooked|приготовленный|đã nấu
boil|варить|luộc
fry|жарить|chiên
bake|печь|nướng
taste|вкус|vị
"""

TIME_MORE = """
Sunday|воскресенье|chủ nhật
Tuesday|вторник|thứ ba
Wednesday|среда|thứ tư
Thursday|четверг|thứ năm
Friday|пятница|thứ sáu
Saturday|суббота|thứ bảy
January|январь|tháng một
February|февраль|tháng hai
March|март|tháng ba
April|апрель|tháng tư
May|май|tháng năm
June|июнь|tháng sáu
July|июль|tháng bảy
August|август|tháng tám
September|сентябрь|tháng chín
October|октябрь|tháng mười
November|ноябрь|tháng mười một
December|декабрь|tháng mười hai
hour|час|giờ
minute|минута|phút
second|секунда|giây
day|день|ngày
night|ночь|đêm
noon|полдень|buổi trưa
midnight|полночь|nửa đêm
weekend|выходные|cuối tuần
holiday|праздник|ngày lễ
birthday|день рождения|sinh nhật
calendar|календарь|lịch
schedule|расписание|lịch trình
early|рано|sớm
late|поздно|muộn
always|всегда|luôn luôn
usually|обычно|thường
often|часто|thường xuyên
sometimes|иногда|thỉnh thoảng
rarely|редко|hiếm khi
never|никогда|không bao giờ
soon|скоро|sớm
already|уже|đã
still|ещё|vẫn
yet|ещё не|chưa
ago|назад|trước đây
"""

NUMBERS_MORE = """
eleven|одиннадцать|mười một
twelve|двенадцать|mười hai
thirteen|тринадцать|mười ba
fourteen|четырнадцать|mười bốn
fifteen|пятнадцать|mười lăm
sixteen|шестнадцать|mười sáu
seventeen|семнадцать|mười bảy
eighteen|восемнадцать|mười tám
nineteen|девятнадцать|mười chín
twenty|двадцать|hai mươi
thirty|тридцать|ba mươi
forty|сорок|bốn mươi
fifty|пятьдесят|năm mươi
sixty|шестьдесят|sáu mươi
seventy|семьдесят|bảy mươi
eighty|восемьдесят|tám mươi
ninety|девяносто|chín mươi
thousand|тысяча|một nghìn
million|миллион|một triệu
half|половина|một nửa
pair|пара|một đôi
third|третий|thứ ba
last one|последний|cuối cùng
"""

WORK = """
job|работа|công việc
work|работа|làm việc
worker|работник|công nhân
boss|начальник|sếp
colleague|коллега|đồng nghiệp
customer|клиент|khách hàng
business|бизнес|kinh doanh
company|компания|công ty
meeting|встреча|cuộc họp
email|почта|email
message|сообщение|tin nhắn
call|звонок|cuộc gọi
salary|зарплата|lương
money|деньги|tiền
price|цена|giá
cheap|дешёвый|rẻ
expensive|дорогой|đắt
free|бесплатный|miễn phí
busy|занятый|bận
break|перерыв|nghỉ
project|проект|dự án
plan|план|kế hoạch
goal|цель|mục tiêu
success|успех|thành công
problem|проблема|vấn đề
idea|идея|ý tưởng
decision|решение|quyết định
team|команда|nhóm
"""

HEALTH = """
health|здоровье|sức khỏe
doctor|врач|bác sĩ
nurse|медсестра|y tá
patient|пациент|bệnh nhân
medicine|лекарство|thuốc
pain|боль|đau
sick|больной|ốm
ill|болен|bệnh
fever|температура|sốt
cough|кашель|ho
cold illness|простуда|cảm lạnh
headache|головная боль|đau đầu
stomachache|боль в животе|đau bụng
injury|травма|chấn thương
blood pressure|давление|huyết áp
hospital|больница|bệnh viện
ambulance|скорая|xe cấp cứu
healthy|здоровый|khỏe
tired|уставший|mệt
sleep|сон|ngủ
rest|отдых|nghỉ ngơi
exercise|упражнение|tập thể dục
sport|спорт|thể thao
run|бегать|chạy
swim|плавать|bơi
walk|ходить|đi bộ
"""

SCHOOL = """
teacher|учитель|giáo viên
student|студент|học sinh
class|класс|lớp
lesson|урок|bài học
homework|домашнее задание|bài tập về nhà
test|тест|bài kiểm tra
exam|экзамен|kỳ thi
grade|оценка|điểm
subject|предмет|môn học
language|язык|ngôn ngữ
word|слово|từ
sentence|предложение|câu
question|вопрос|câu hỏi
answer|ответ|câu trả lời
meaning|значение|nghĩa
dictionary|словарь|từ điển
example|пример|ví dụ
practice|практика|luyện tập
memory|память|trí nhớ
mistake|ошибка|lỗi
correct|правильный|đúng
wrong|неправильный|sai
easy|лёгкий|dễ
difficult|трудный|khó
smart|умный|thông minh
learn|учить|học
teach|преподавать|dạy
explain|объяснять|giải thích
understand|понимать|hiểu
"""

CITY_LIFE = """
traffic|трафик|giao thông
car|машина|xe hơi
bike|велосипед|xe đạp
motorbike|мотоцикл|xe máy
truck|грузовик|xe tải
driver|водитель|tài xế
passenger|пассажир|hành khách
map|карта|bản đồ
direction|направление|hướng
north|север|bắc
south|юг|nam
east|восток|đông
west|запад|tây
center|центр|trung tâm
corner|угол|góc
crosswalk|пешеходный переход|vạch sang đường
traffic light|светофор|đèn giao thông
parking|парковка|chỗ đậu xe
gasoline|бензин|xăng
helmet|шлем|mũ bảo hiểm
seat|место|chỗ ngồi
queue|очередь|hàng đợi
crowd|толпа|đám đông
noise|шум|tiếng ồn
quiet|тихий|yên tĩnh
safe|безопасный|an toàn
dangerous|опасный|nguy hiểm
"""

OBJECTS = """
thing|вещь|đồ vật
object|предмет|vật
tool|инструмент|dụng cụ
machine|машина|máy
camera|камера|máy ảnh
photo|фото|ảnh
picture|картинка|hình
video|видео|video
music|музыка|nhạc
song|песня|bài hát
movie|фильм|phim
game|игра|trò chơi
ball|мяч|bóng
toy|игрушка|đồ chơi
gift|подарок|quà
card|открытка|thiệp
letter|письмо|thư
stamp|марка|tem
ticket|билет|vé
passport|паспорт|hộ chiếu
id card|удостоверение|thẻ căn cước
document|документ|tài liệu
file|файл|tệp
screen|экран|màn hình
keyboard|клавиатура|bàn phím
mouse device|мышь|chuột máy tính
battery|батарейка|pin
charger|зарядка|bộ sạc
cable|кабель|dây cáp
password|пароль|mật khẩu
website|сайт|trang web
internet|интернет|internet
app|приложение|ứng dụng
"""

VERBS_MORE = """
begin|начинать|bắt đầu
finish|заканчивать|kết thúc
continue|продолжать|tiếp tục
change|менять|thay đổi
choose|выбирать|chọn
decide|решать|quyết định
hope|надеяться|hy vọng
wish|желать|ước
worry|волноваться|lo lắng
laugh|смеяться|cười
cry|плакать|khóc
shout|кричать|la
whisper|шептать|thì thầm
touch|трогать|chạm
hold|держать|cầm
push|толкать|đẩy
pull|тянуть|kéo
cut|резать|cắt
break|ломать|làm vỡ
fix|чинить|sửa
build|строить|xây
move|двигаться|di chuyển
turn|поворачивать|quay
follow|следовать|theo
lead|вести|dẫn
win|побеждать|thắng
fail|проигрывать|thất bại
grow|расти|lớn lên
die|умирать|chết
live|жить|sống
happen|случаться|xảy ra
seem|казаться|có vẻ
become|становиться|trở thành
remain|оставаться|còn lại
include|включать|bao gồm
allow|разрешать|cho phép
refuse|отказываться|từ chối
agree|соглашаться|đồng ý
disagree|не соглашаться|không đồng ý
compare|сравнивать|so sánh
describe|описывать|mô tả
imagine|представлять|tưởng tượng
notice|замечать|nhận thấy
realize|осознавать|nhận ra
believe|верить|tin
doubt|сомневаться|nghi ngờ
trust|доверять|tin tưởng
share|делиться|chia sẻ
borrow|занимать|mượn
lend|давать в долг|cho mượn
owe|быть должным|nợ
save|сохранять|tiết kiệm
spend|тратить|tiêu
cost|стоить|có giá
count|считать|đếm
measure|измерять|đo
weigh|весить|cân
fill|наполнять|làm đầy
empty|опустошать|làm trống
cover|накрывать|che
discover|открывать|khám phá
invent|изобретать|phát minh
create|создавать|tạo
design|проектировать|thiết kế
draw|рисовать|vẽ
paint|красить|sơn
sing|петь|hát
dance|танцевать|nhảy
cook|готовить|nấu
clean|убирать|dọn
wash|мыть|rửa
dry|сушить|phơi
hang|вешать|treo
throw|бросать|ném
catch|ловить|bắt
kick|бить ногой|đá
hit|ударять|đánh
jump|прыгать|nhảy
climb|лезть|leo
drive|водить|lái
ride|ехать|cưỡi / đi
fly|летать|bay
sail|плыть на судне|đi thuyền
travel|путешествовать|du lịch
visit|посещать|thăm
arrive|приезжать|đến
return|возвращаться|trở về
enter|входить|vào
exit|выходить|ra
send|отправлять|gửi
receive|получать|nhận
deliver|доставлять|giao
order|заказывать|đặt
book a table|бронировать|đặt chỗ
cancel|отменять|hủy
check|проверять|kiểm tra
search|искать|tìm kiếm
download|скачивать|tải xuống
upload|загружать|tải lên
print|печатать|in
copy|копировать|sao chép
delete|удалять|xóa
edit|редактировать|chỉnh sửa
"""

ADJ_MORE = """
strong|сильный|mạnh
weak|слабый|yếu
heavy|тяжёлый|nặng
light weight|лёгкий|nhẹ
long|длинный|dài
short|короткий|ngắn
tall|высокий|cao
wide|широкий|rộng
narrow|узкий|hẹp
deep|глубокий|sâu
thick|толстый|dày
thin|тонкий|mỏng
soft|мягкий|mềm
hard material|твёрдый|cứng
smooth|гладкий|mịn
rough|шершавый|nhám
sharp|острый|sắc
flat|плоский|phẳng
round|круглый|tròn
square shape|квадратный|vuông
rich|богатый|giàu
poor|бедный|nghèo
famous|известный|nổi tiếng
popular|популярный|phổ biến
special|особенный|đặc biệt
normal|обычный|bình thường
strange|странный|lạ
funny|смешной|buồn cười
serious|серьёзный|nghiêm túc
kind|добрый|tử tế
polite|вежливый|lịch sự
rude|грубый|thô lỗ
honest|честный|thành thật
brave|храбрый|dũng cảm
afraid|испуганный|sợ
angry|злой|tức giận
calm|спокойный|bình tĩnh
proud|гордый|tự hào
lonely|одинокий|cô đơn
bored|скучающий|chán
excited|взволнованный|hào hứng
surprised|удивлённый|ngạc nhiên
interested|заинтересованный|thích thú
useful|полезный|hữu ích
useless|бесполезный|vô ích
possible|возможный|có thể
impossible|невозможный|không thể
necessary|необходимый|cần thiết
available|доступный|có sẵn
comfortable|удобный|thoải mái
convenient|удобный по месту|tiện
dangerous|опасный|nguy hiểm
safe|безопасный|an toàn
legal|законный|hợp pháp
illegal|незаконный|bất hợp pháp
public|общественный|công cộng
private|частный|riêng tư
local|местный|địa phương
international|международный|quốc tế
modern|современный|hiện đại
traditional|традиционный|truyền thống
natural|натуральный|tự nhiên
real|настоящий|thật
fake|поддельный|giả
true|правдивый|đúng
false|ложный|sai
main|главный|chính
simple|простой|đơn giản
complex|сложный|phức tạp
clear|ясный|rõ
sure|уверенный|chắc chắn
lucky|удачливый|may mắn
"""

ADVERBS = """
very|очень|rất
too|слишком|quá
quite|довольно|khá
almost|почти|gần như
enough|достаточно|đủ
also|также|cũng
only|только|chỉ
even|даже|thậm chí
just|только что|vừa
already|уже|đã
still|всё ещё|vẫn
again|снова|lại
together|вместе|cùng nhau
alone|один|một mình
here|здесь|ở đây
there|там|ở đó
everywhere|везде|mọi nơi
somewhere|где-то|đâu đó
away|прочь|đi
inside|внутри|bên trong
outside|снаружи|bên ngoài
upstairs|наверху|tầng trên
downstairs|внизу|tầng dưới
forward|вперёд|về phía trước
backward|назад|về phía sau
quickly|быстро|nhanh
slowly|медленно|chậm
carefully|осторожно|cẩn thận
easily|легко|dễ dàng
suddenly|внезапно|đột nhiên
finally|наконец|cuối cùng
recently|недавно|gần đây
currently|сейчас|hiện nay
probably|вероятно|có lẽ
maybe|возможно|có thể
really|действительно|thật sự
especially|особенно|đặc biệt
"""

FEELINGS = """
love|любовь|tình yêu
hate|ненависть|ghét
fear|страх|nỗi sợ
joy|радость|niềm vui
sadness|грусть|nỗi buồn
anger|гнев|cơn giận
hope|надежда|hy vọng
peace|мир|hòa bình
stress|стресс|căng thẳng
energy|энергия|năng lượng
feeling|чувство|cảm giác
emotion|эмоция|cảm xúc
mood|настроение|tâm trạng
dream|мечта|giấc mơ
thought|мысль|suy nghĩ
opinion|мнение|ý kiến
reason|причина|lý do
result|результат|kết quả
"""

PHRASES = """
I don't know|Я не знаю|Tôi không biết
I think so|Я думаю да|Tôi nghĩ vậy
I agree|Я согласен|Tôi đồng ý
I disagree|Я не согласен|Tôi không đồng ý
Of course|Конечно|Dĩ nhiên
No problem|Нет проблем|Không sao
See you later|Увидимся позже|Hẹn gặp lại sau
Take a seat|Садитесь|Mời ngồi
Come in|Входите|Mời vào
Just a moment|Одну минуту|Chờ một chút
I am ready|Я готов|Tôi sẵn sàng
I am busy|Я занят|Tôi đang bận
I am tired|Я устал|Tôi mệt
I am lost|Я заблудился|Tôi bị lạc
Can you help me|Можете помочь|Bạn giúp tôi được không
Where are you from|Откуда вы|Bạn đến từ đâu
I am from Russia|Я из России|Tôi đến từ Nga
I am from Vietnam|Я из Вьетнама|Tôi đến từ Việt Nam
I live here|Я живу здесь|Tôi sống ở đây
How old are you|Сколько тебе лет|Bạn bao nhiêu tuổi
Nice weather|Хорошая погода|Thời tiết đẹp
It's raining|Идёт дождь|Trời đang mưa
I like it|Мне нравится|Tôi thích cái này
I don't like it|Мне не нравится|Tôi không thích
What happened|Что случилось|Chuyện gì xảy ra
Never mind|Неважно|Không sao đâu
Good job|Молодец|Làm tốt lắm
Be careful|Осторожно|Cẩn thận
Hurry up|Поторопись|Nhanh lên
Wait for me|Подожди меня|Chờ tôi với
Call me|Позвони мне|Gọi cho tôi
Text me|Напиши мне|Nhắn tin cho tôi
See you soon|До скорой встречи|Hẹn gặp sớm
Have a nice day|Хорошего дня|Chúc một ngày tốt lành
"""


def extra_specs() -> list[tuple[str, str, int, dict, list]]:
    def t(en: str, ru: str, vi: str) -> dict:
        return {"en": en, "ru": ru, "vi": vi}

    return [
        ("88_family.json", "family", 88, t("Family", "Семья", "Gia đình"), extra_rows("fam", FAMILY)),
        ("89_home.json", "home", 89, t("Home", "Дом", "Nhà"), extra_rows("home", HOME)),
        ("90_body.json", "body", 90, t("Body", "Тело", "Cơ thể"), extra_rows("body", BODY)),
        ("91_clothes.json", "clothes", 91, t("Clothes", "Одежда", "Quần áо"), extra_rows("cl", CLOTHES)),
        ("92_colors.json", "colors", 92, t("Colors", "Цвета", "Màu sắc"), extra_rows("col", COLORS)),
        ("93_weather.json", "weather", 93, t("Weather", "Погода", "Thời tiết"), extra_rows("wx", WEATHER)),
        ("94_places.json", "places", 94, t("Places", "Места", "Địa điểm"), extra_rows("pl", PLACES)),
        ("95_animals.json", "animals", 95, t("Animals", "Животные", "Động vật"), extra_rows("an", ANIMALS)),
        ("96_nature.json", "nature", 96, t("Nature", "Природа", "Thiên nhiên"), extra_rows("nat", NATURE)),
        ("97a_food_more.json", "food", 82, t("Food", "Еда", "Đồ ăn"), extra_rows("foodx", FOOD_MORE)),
        ("97b_time_more.json", "time", 86, t("Time", "Время", "Thời gian"), extra_rows("timex", TIME_MORE)),
        ("97c_numbers_more.json", "numbers", 81, t("Numbers", "Числа", "Số"), extra_rows("numx", NUMBERS_MORE)),
        ("97d_work.json", "work", 97, t("Work", "Работа", "Công việc"), extra_rows("work", WORK)),
        ("97e_health.json", "health", 98, t("Health", "Здоровье", "Sức khỏe"), extra_rows("hl", HEALTH)),
        ("97f_school.json", "school", 100, t("School", "Учёба", "Học tập"), extra_rows("sch", SCHOOL)),
        ("97g_city.json", "city", 101, t("City", "Город", "Thành phố"), extra_rows("city", CITY_LIFE)),
        ("97h_objects.json", "objects", 102, t("Objects", "Предметы", "Đồ vật"), extra_rows("obj", OBJECTS)),
        ("97i_verbs_more.json", "verbs", 60, t("Verbs", "Глаголы", "Động từ"), extra_rows("verbx", VERBS_MORE)),
        ("97j_adj_more.json", "adjectives", 70, t("Adjectives", "Прилагательные", "Tính từ"), extra_rows("adjx", ADJ_MORE)),
        ("97k_adverbs.json", "adverbs", 103, t("Adverbs", "Наречия", "Trạng từ"), extra_rows("adv", ADVERBS)),
        ("97l_feelings.json", "feelings", 104, t("Feelings", "Чувства", "Cảm xúc"), extra_rows("feel", FEELINGS)),
        ("97m_phrases.json", "phrases", 105, t("Useful phrases", "Полезные фразы", "Câu hữu ích"), extra_rows("phrx", PHRASES)),
    ]
