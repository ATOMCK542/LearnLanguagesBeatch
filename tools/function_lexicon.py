"""Closed-class additions: prepositions first, then conjunctions, determiners, particles, semi-modals."""
from __future__ import annotations


def lines(block: str) -> list[tuple[str, str, str]]:
    rows = []
    for raw in block.strip().splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        en, ru, vi = [p.strip() for p in line.split("|", 2)]
        rows.append((en, ru, vi))
    return rows


# Existing checklist ids stay stable.
PREP_IDS = {
    "in": "fn.in",
    "on": "fn.on",
    "at": "fn.at",
    "to": "fn.to",
    "from": "fn.from",
    "with": "fn.with",
    "for": "fn.for",
    "of": "fn.of",
    "about": "fn.about",
    "by": "fn.by",
    "into": "fn.into",
    "out of": "fn.out_of",
    "up": "fn.up",
    "down": "fn.down",
    "over": "fn.over",
    "under": "fn.under",
    "between": "fn.between",
    "without": "fn.without",
    "before": "fn.before",
    "after": "fn.after",
    "during": "fn.during",
}

# Simple and complex prepositions. One primary sense, short gloss.
PREPOSITIONS = """
aboard|на борту|trên tàu
about|о / около|về / khoảng
above|над / выше|phía trên
across|через|qua
after|после / после того как|sau
against|против|chống lại
ahead of|впереди|phía trước
along|вдоль|dọc theo
alongside|рядом / вдоль|dọc theo / bên cạnh
amid|среди|giữa
amidst|посреди|giữa
among|среди|trong số
amongst|среди|trong số
apart from|помимо|ngoài
around|вокруг|quanh
as|как / в качестве|như
as for|что касается|còn về
as of|по состоянию на|tính đến
as to|относительно|về việc
aside from|помимо|ngoài ra
at|у / в (точка)|ở / lúc
atop|на вершине|trên đỉnh
away from|прочь от|xa khỏi
because of|из-за|vì
before|до / перед|trước
behind|позади|phía sau
below|ниже / под|bên dưới
beneath|под|bên dưới
beside|рядом с|bên cạnh
besides|кроме|ngoài ra
between|между|giữa
beyond|за пределами|bên kia
but for|если бы не|nếu không có
by|у / кем / к|bởi / cạnh
by means of|с помощью|bằng cách
circa|около (о дате)|khoảng
close to|близко к|gần
concerning|относительно|về
considering|учитывая|xét về
despite|несмотря на|mặc dù
down|вниз / по|xuống
due to|из-за|do
during|во время|trong lúc
except|кроме|ngoại trừ
except for|за исключением|ngoài trừ
excluding|исключая|không kể
far from|далеко от|xa
following|вслед за|sau
for|для / за|cho / vì
from|из / от|từ
in|в|trong / ở
in addition to|в дополнение к|ngoài ra
in front of|перед|phía trước
in spite of|несмотря на|mặc dù
including|включая|bao gồm
inside|внутри|bên trong
instead of|вместо|thay vì
into|внутрь|vào trong
like|как / подобно|như
minus|минус / без|trừ
near|около / рядом|gần
next to|рядом с|bên cạnh
notwithstanding|несмотря на|bất chấp
of|род. падеж / of|của
off|с / прочь|khỏi
on|на|trên
on account of|по причине|vì
on behalf of|от имени|thay mặt
on top of|сверху|trên
onto|на поверхность|lên
opposite|напротив|đối diện
out|наружу|ra ngoài
out of|из|ra khỏi
outside|снаружи|bên ngoài
over|над / через|qua / phía trên
owing to|из-за|do
past|мимо / после|qua / quá
pending|в ожидании|trong khi chờ
per|на / за единицу|mỗi
plus|плюс|cộng
prior to|до|trước khi
regarding|относительно|về việc
regardless of|независимо от|bất kể
round|вокруг|quanh
since|с (момента)|từ khi
than|чем|hơn
thanks to|благодаря|nhờ
through|через / сквозь|xuyên qua
throughout|на протяжении|suốt
till|до|cho đến
to|к / в|đến / tới
together with|вместе с|cùng với
toward|по направлению к|về phía
towards|по направлению к|về phía
under|под|dưới
underneath|под|bên dưới
unlike|в отличие от|không giống
until|до тех пор|cho đến khi
up|вверх|lên
up to|вплоть до|lên đến
upon|на / по|trên / khi
versus|против|so với
via|через|qua
with|с|với
within|в пределах|trong vòng
without|без|không có
worth|стоящий|đáng
according to|согласно|theo
ahead of time|заранее|trước giờ
along with|вместе с|cùng với
as opposed to|в отличие от|trái với
as well as|а также|cũng như
at the end of|в конце|vào cuối
at the beginning of|в начале|vào đầu
away from home|вдали от дома|xa nhà
by virtue of|в силу|nhờ vào
by way of|путём|bằng đường
for the sake of|ради|vì
in accordance with|в соответствии с|theo đúng
in back of|позади|phía sau
in between|между|ở giữa
in case of|в случае|trong trường hợp
in charge of|ответственный за|phụ trách
in comparison with|по сравнению с|so với
in contrast to|в отличие от|trái với
in favor of|в пользу|ủng hộ
in lieu of|вместо|thay cho
in need of|нуждающийся в|cần
in place of|вместо|thay cho
in relation to|в отношении|liên quan đến
in search of|в поисках|đi tìm
in terms of|с точки зрения|về mặt
in the course of|в ходе|trong quá trình
in the face of|перед лицом|trước
in the middle of|посреди|ở giữa
in view of|ввиду|xét vì
inside of|внутри|bên trong
off of|с|khỏi
on the basis of|на основе|trên cơ sở
on the part of|со стороны|về phía
on the verge of|на грани|sắp
out from|из|từ trong
outside of|за пределами|bên ngoài
rather than|а не / вместо того чтобы|hơn là
such as|такой как|chẳng hạn như
with regard to|что касается|liên quan đến
with respect to|относительно|đối với
with the exception of|за исключением|ngoại trừ
as a result of|в результате|do kết quả
for the purpose of|с целью|nhằm
at the expense of|за счёт|bằng cái giá
"""

# Usage frames so a preposition is learned in a phrase, not as a bare gloss.
PREP_PHRASES = """
aboard the ship|на корабле|trên tàu
about this book|об этой книге|về cuốn sách này
above the clouds|над облаками|trên những đám mây
across the street|через улицу|qua đường
after lunch|после обеда|sau bữa trưa
against the wall|к стене|dựa vào tường
along the river|вдоль реки|dọc theo sông
alongside the road|вдоль дороги|dọc theo đường
among friends|среди друзей|giữa bạn bè
around the city|по городу|quanh thành phố
as a teacher|как учитель|với tư cách giáo viên
at the station|на вокзале|ở nhà ga
atop the hill|на вершине холма|trên đỉnh đồi
away from the fire|подальше от огня|xa ngọn lửa
because of the rain|из-за дождя|vì mưa
before noon|до полудня|trước buổi trưa
behind the door|за дверью|phía sau cửa
below zero|ниже нуля|dưới không độ
beneath the tree|под деревом|dưới gốc cây
beside me|рядом со мной|bên cạnh tôi
besides that|кроме этого|ngoài ra
between us|между нами|giữa chúng ta
beyond the hill|за холмом|bên kia đồi
by the window|у окна|cạnh cửa sổ
by means of a key|с помощью ключа|bằng chìa khóa
circa nineteen ninety|около тысяча девятьсот девяностого|khoảng năm một chín chín không
close to the station|близко к станции|gần nhà ga
concerning the plan|насчёт плана|về kế hoạch
despite the rain|несмотря на дождь|mặc dù trời mưa
down the stairs|вниз по лестнице|xuống cầu thang
due to traffic|из-за пробки|do kẹt xe
during the day|в течение дня|trong ngày
except me|кроме меня|ngoại trừ tôi
except for Monday|кроме понедельника|ngoại trừ thứ hai
for you|для тебя|cho bạn
from home|из дома|từ nhà
in the room|в комнате|trong phòng
in addition to rice|кроме риса|ngoài cơm
in front of the house|перед домом|phía trước nhà
in spite of the pain|несмотря на боль|mặc dù đau
including tax|включая налог|bao gồm thuế
inside the box|внутри коробки|bên trong hộp
instead of coffee|вместо кофе|thay vì cà phê
into the water|в воду|vào nước
like a child|как ребёнок|như một đứa trẻ
near the bank|рядом с банком|gần ngân hàng
next to the door|рядом с дверью|bên cạnh cửa
of the day|этого дня|của ngày
off the bus|с автобуса|xuống xe buýt
on the table|на столе|trên bàn
on behalf of the team|от имени команды|thay mặt đội
on top of the fridge|на холодильнике|trên tủ lạnh
onto the roof|на крышу|lên mái nhà
opposite the bank|напротив банка|đối diện ngân hàng
out of the house|из дома|ra khỏi nhà
outside the city|за городом|bên ngoài thành phố
over the bridge|через мост|qua cầu
owing to the storm|из-за шторма|do bão
past the shop|мимо магазина|đi qua cửa hàng
per person|с человека|mỗi người
plus tax|плюс налог|cộng thuế
prior to arrival|до прибытия|trước khi đến
regarding work|насчёт работы|về công việc
regardless of age|независимо от возраста|bất kể tuổi tác
round the corner|за углом|quanh góc
since Monday|с понедельника|từ thứ hai
than me|чем я|hơn tôi
thanks to you|благодаря тебе|nhờ bạn
through the park|через парк|qua công viên
throughout the year|весь год|suốt năm
till night|до ночи|đến đêm
to school|в школу|đến trường
together with friends|вместе с друзьями|cùng bạn bè
toward the sea|к морю|về phía biển
under the table|под столом|dưới bàn
underneath the bed|под кроватью|bên dưới giường
unlike him|в отличие от него|không giống anh ấy
until tomorrow|до завтра|cho đến ngày mai
up the hill|вверх на холм|lên đồi
up to ten|до десяти|lên đến mười
upon arrival|по прибытии|khi đến nơi
via email|по почте|qua email
with me|со мной|với tôi
within an hour|в течение часа|trong vòng một giờ
without sugar|без сахара|không có đường
worth the price|стоит этих денег|đáng số tiền đó
according to the map|согласно карте|theo bản đồ
ahead of us|впереди нас|phía trước chúng ta
along with this letter|вместе с этим письмом|cùng với lá thư này
apart from that|помимо этого|ngoài điều đó
as for me|что касается меня|còn tôi
as of today|на сегодняшний день|tính đến hôm nay
as opposed to tea|в отличие от чая|trái với trà
aside from this|кроме этого|ngoài cái này
at the end of the day|в конце дня|vào cuối ngày
at the beginning of May|в начале мая|vào đầu tháng năm
because of you|из-за тебя|vì bạn
far from here|далеко отсюда|xa đây
following the meeting|после собрания|sau cuộc họp
for the sake of peace|ради мира|vì hòa bình
in case of fire|при пожаре|trong trường hợp cháy
in charge of the shop|заведует магазином|phụ trách cửa hàng
in favor of the plan|за этот план|ủng hộ kế hoạch
in need of help|нуждается в помощи|cần sự giúp đỡ
in place of him|вместо него|thay cho anh ấy
in search of a job|в поисках работы|đi tìm việc
in terms of money|с точки зрения денег|về mặt tiền
in the middle of the night|посреди ночи|giữa đêm
instead of me|вместо меня|thay vì tôi
on account of illness|из-за болезни|vì ốm
out of money|без денег|hết tiền
rather than wait|вместо того чтобы ждать|hơn là chờ
such as tea|например чай|chẳng hạn như trà
with regard to the price|что касается цены|về giá cả
in accordance with the law|по закону|theo luật
on the basis of facts|на основе фактов|dựa trên sự thật
in contrast to him|в отличие от него|trái ngược với anh ấy
at home|дома|ở nhà
at work|на работе|ở chỗ làm
at school|в школе|ở trường
in bed|в постели|trên giường
on time|вовремя|đúng giờ
in time|успеть вовремя|kịp giờ
at night|ночью|vào ban đêm
in the morning|утром|vào buổi sáng
in the evening|вечером|vào buổi tối
at noon|в полдень|vào buổi trưa
at midnight|в полночь|vào nửa đêm
on Monday|в понедельник|vào thứ hai
in May|в мае|vào tháng năm
by bus|на автобусе|bằng xe buýt
by car|на машине|bằng ô tô
by train|на поезде|bằng tàu hỏa
by plane|на самолёте|bằng máy bay
on foot|пешком|đi bộ
on the phone|по телефону|qua điện thoại
in English|по-английски|bằng tiếng Anh
in Russian|по-русски|bằng tiếng Nga
in Vietnamese|по-вьетнамски|bằng tiếng Việt
at least|по крайней мере|ít nhất
at most|самое большее|nhiều nhất
at once|сразу|ngay lập tức
at first|сначала|lúc đầu
at last|наконец|cuối cùng
in fact|на самом деле|thật ra
in general|в целом|nói chung
in particular|в частности|nói riêng
in public|публично|nơi công cộng
in private|наедине|riêng tư
on purpose|нарочно|cố ý
by accident|случайно|tình cờ
by chance|случайно|ngẫu nhiên
by heart|наизусть|thuộc lòng
on sale|на распродаже|đang giảm giá
for sale|продаётся|để bán
for example|например|ví dụ
for instance|к примеру|chẳng hạn
for free|бесплатно|miễn phí
for now|пока|tạm thời
for good|навсегда|mãi mãi
as usual|как обычно|như thường lệ
at all|совсем / вообще|chút nào
in addition|кроме того|ngoài ra
in advance|заранее|trước
in detail|подробно|chi tiết
in turn|в свою очередь|đến lượt
on average|в среднем|trung bình
on the whole|в целом|nhìn chung
out of date|устарело|lỗi thời
up to date|актуально|cập nhật
out of order|не работает|hỏng
under control|под контролем|trong tầm kiểm soát
on the contrary|наоборот|trái lại
on the other hand|с другой стороны|mặt khác
in other words|другими словами|nói cách khác
at the same time|в то же время|đồng thời
from time to time|время от времени|thỉnh thoảng
all of a sudden|вдруг|đột nhiên
in the meantime|тем временем|trong lúc đó
at present|сейчас|hiện nay
in the past|в прошлом|trong quá khứ
in the future|в будущем|trong tương lai
on the way|по пути|trên đường
in the way|мешает|cản đường
by the way|кстати|nhân tiện
in a hurry|в спешке|đang vội
in trouble|в беде|gặp rắc rối
in danger|в опасности|gặp nguy hiểm
in love|влюблён|đang yêu
on fire|горит|đang cháy
on duty|на дежурстве|đang trực
off duty|не на службе|hết ca
from now on|отныне|từ nay trở đi
since then|с тех пор|từ đó
until now|до сих пор|cho đến bây giờ
at risk|под угрозой|có nguy cơ
in common|общего|chung
under construction|строится|đang xây dựng
on holiday|в отпуске|đang nghỉ
in touch|на связи|giữ liên lạc
out of touch|без связи|mất liên lạc
"""

CONJUNCTIONS = """
nor|ни|cũng không
yet|однако|tuy vậy
although|хотя|mặc dù
though|хотя|dù
even though|даже хотя|ngay cả khi
while|пока / тогда как|trong khi
whereas|тогда как|trong khi đó
whether|ли|liệu
unless|если не|trừ khi
once|как только / один раз|một khi / một lần
so that|чтобы|để mà
in order to|чтобы|để
in order that|чтобы|để cho
even if|даже если|ngay cả nếu
as if|как будто|như thể
as though|как будто|y như thể
provided|при условии что|miễn là
providing|при условии что|miễn là
whenever|когда бы ни|bất cứ khi nào
wherever|где бы ни|bất cứ nơi nào
whoever|кто бы ни|bất cứ ai
whatever|что бы ни|bất cứ điều gì
whichever|какой бы ни|bất cứ cái nào
however|однако / как бы ни|tuy nhiên
therefore|поэтому|do đó
moreover|более того|hơn nữa
nevertheless|тем не менее|tuy nhiên
nonetheless|тем не менее|dù vậy
otherwise|иначе|nếu không thì
meanwhile|тем временем|trong khi đó
furthermore|кроме того|hơn nữa
consequently|следовательно|do đó
thus|таким образом|như vậy
hence|следовательно|vì thế
anyway|всё равно|dù sao
anyhow|так или иначе|dù thế nào
lest|чтобы не|kẻo
now that|теперь когда|giờ thì
as soon as|как только|ngay khi
as long as|пока / если только|miễn là
whether or not|независимо от того|dù có hay không
not only|не только|không chỉ
but also|но и|mà còn
either or|либо … либо|hoặc … hoặc
neither nor|ни … ни|không … cũng không
both and|и … и|vừa … vừa
if only|если бы|giá mà
so as to|чтобы|để
in case|в случае если|trong trường hợp
supposing|предположим что|giả sử
given that|учитывая что|vì rằng
seeing that|поскольку|bởi vì
as far as|насколько|về phần
as soon as possible|как можно скорее|càng sớm càng tốt
and so on|и так далее|vân vân
and so forth|и тому подобное|vân vân
or else|а не то|nếu không thì
"""

DETERMINERS = """
none|ни один / никто|không ai / không cái nào
several|несколько|vài
few|мало|ít
little|немного / мало|ít
fewer|меньше|ít hơn
less|меньше|ít hơn
more|больше|nhiều hơn
most|большинство / самый|hầu hết / nhất
least|наименьший|ít nhất
another|ещё один / другой|một cái khác
other|другой|khác
others|другие|những cái khác
such|такой|như vậy
own|свой|của chính mình
various|различные|nhiều loại
certain|некоторый / определённый|nhất định
whole|весь|toàn bộ
either|любой из двух|một trong hai
neither|ни один из двух|không cái nào
double|двойной|gấp đôi
twice|дважды|hai lần
thrice|трижды|ba lần
lots of|много|rất nhiều
plenty of|полно|dồi dào
a bit of|немного|một chút
a number of|ряд|một số
a couple of|пара|một cặp
a great deal of|очень много|rất nhiều
enough of|достаточно|đủ
half of|половина|một nửa
all of|все из|tất cả
some of|некоторые из|một số
none of|никто из|không ai trong
each of|каждый из|mỗi
both of|оба|cả hai
another one|ещё один|một cái nữa
the other|другой|cái kia
the same|тот же|cùng một
no such|такого нет|không có cái như vậy
what a|какой|thật là một
quite a|довольно|khá là
many a|не один|nhiều
"""

PARTICLES = """
else|ещё / другой|khác
ever|когда-либо|từng
hardly|едва|hầu như không
barely|едва|chỉ vừa đủ
merely|всего лишь|chỉ là
simply|просто|chỉ đơn giản
exactly|точно|chính xác
precisely|именно|chính xác
approximately|приблизительно|khoảng chừng
perhaps|возможно|có lẽ
possibly|возможно|có khả năng
surely|наверняка|chắc chắn
indeed|в самом деле|quả thật
certainly|конечно|chắc chắn
definitely|определённо|chắc chắn
absolutely|абсолютно|hoàn toàn
yeah|да|ừ
yep|ага|ừ
nope|неа|không
alright|ладно|được rồi
oh|о|ồ
ah|ах|à
um|эм|ờ
uh|э|ờ
hey|эй|này
wow|ух ты|ồ
ouch|ой|ái
oops|ой|ôi
hmm|хм|hừm
well|ну|à / thôi
etcetera|и так далее|vân vân
for example|например|ví dụ
that is|то есть|nghĩa là
namely|а именно|cụ thể là
also known as|также известный как|còn gọi là
so to speak|так сказать|nói cách khác
kind of|вроде|hơi
sort of|вроде как|kiểu như
at any rate|во всяком случае|dù sao đi nữa
after all|в конце концов|suy cho cùng
above all|прежде всего|trên hết
in short|короче|tóm lại
of course not|конечно нет|dĩ nhiên là không
not at all|совсем нет|không có gì
no longer|больше не|không còn
any longer|больше не|nữa
any more|больше|nữa
once again|ещё раз|một lần nữa
once more|ещё раз|một lần nữa
all right|хорошо|được
as yet|пока ещё|cho đến nay
just now|только что|vừa mới
right now|прямо сейчас|ngay bây giờ
so far|пока что|cho đến nay
as yet not|ещё нет|vẫn chưa
let alone|не говоря уже|chưa kể
not to mention|не говоря о|chưa kể đến
as a rule|как правило|theo lệ
in any case|в любом случае|dù sao cũng
no matter|неважно|bất kể
no matter what|что бы ни было|dù thế nào
no matter how|как бы ни|dù ... thế nào
more or less|более или менее|ít nhiều
sooner or later|рано или поздно|sớm muộn gì
by and large|в общем|nhìn chung
and yet|и всё же|vậy mà
even so|даже так|dù vậy
if so|если так|nếu vậy
if not|если нет|nếu không
or so|или около того|hoặc khoảng
and then|а потом|rồi thì
back|назад / обратно|lại / về
forth|вперёд|ra
apart|порознь|riêng ra
aside|в сторону|sang một bên
instead|вместо этого|thay vào đó
alone|только / один|chỉ / một mình
elsewise|иначе|cách khác
nowhere near|далеко не|còn lâu
not quite|не совсем|không hẳn
not yet|ещё нет|chưa
not only that|мало того|không chỉ vậy
as it were|так сказать|có thể nói
so much|так много|nhiều đến thế
too much|слишком много|quá nhiều
too many|слишком много|quá nhiều
so many|так много|nhiều đến thế
"""

# en|ru|vi|theme  theme is be_do_have or modals
AUX_EXTRA = """
dare|сметь|dám|modals
daren't|не смею|không dám|modals
needn't|не нужно|không cần|modals
mustn't|нельзя / не должен|không được|modals
shouldn't|не следует|không nên|modals
couldn't|не мог|không thể|modals
wouldn't|не стал бы|sẽ không|modals
mightn't|возможно, не|có lẽ không|modals
shan't|не shall|sẽ không|modals
hadn't|не had|đã không|be_do_have
haven't got|нет (брит.)|không có|be_do_have
have to|должен / приходится|phải|modals
has to|должен (он)|phải|modals
had to|пришлось|đã phải|modals
going to|собираться|sắp / sẽ|modals
gonna|собираюсь (разг.)|sắp|modals
wanna|хочу (разг.)|muốn|modals
gotta|надо (разг.)|phải|modals
be able to|быть в состоянии|có khả năng|modals
had better|лучше бы|nên|modals
would rather|предпочёл бы|thích hơn|modals
be supposed to|должен по плану|lẽ ra phải|modals
be about to|вот-вот|sắp sửa|modals
let's|давай(те)|chúng ta hãy|modals
I'm|я (есть)|tôi là|be_do_have
you're|ты / вы (есть)|bạn là|be_do_have
he's|он (есть)|anh ấy là|be_do_have
she's|она (есть)|cô ấy là|be_do_have
it's|это (есть)|nó là|be_do_have
we're|мы (есть)|chúng tôi là|be_do_have
they're|они (есть)|họ là|be_do_have
I've|я уже|tôi đã|be_do_have
you've|ты уже|bạn đã|be_do_have
we've|мы уже|chúng tôi đã|be_do_have
they've|они уже|họ đã|be_do_have
I'd|я бы|tôi sẽ|modals
you'd|ты бы|bạn sẽ|modals
he'd|он бы|anh ấy sẽ|modals
she'd|она бы|cô ấy sẽ|modals
we'd|мы бы|chúng tôi sẽ|modals
they'd|они бы|họ sẽ|modals
I'll|я буду|tôi sẽ|modals
you'll|ты будешь|bạn sẽ|modals
he'll|он будет|anh ấy sẽ|modals
she'll|она будет|cô ấy sẽ|modals
we'll|мы будем|chúng tôi sẽ|modals
they'll|они будут|họ sẽ|modals
there's|имеется|có|be_do_have
here's|вот|đây là|be_do_have
that's|это|đó là|be_do_have
what's|что (есть)|là gì|be_do_have
who's|кто (есть)|là ai|be_do_have
where's|где (есть)|ở đâu|be_do_have
when's|когда (есть)|khi nào|be_do_have
how's|как (есть)|thế nào|be_do_have
why's|почему|tại sao|be_do_have
I'm not|я не|tôi không phải|be_do_have
you're not|ты не|bạn không phải|be_do_have
we're not|мы не|chúng tôi không|be_do_have
they're not|они не|họ không|be_do_have
I've got|у меня есть|tôi có|be_do_have
you've got|у тебя есть|bạn có|be_do_have
we've got|у нас есть|chúng tôi có|be_do_have
they've got|у них есть|họ có|be_do_have
I'd like|я бы хотел|tôi muốn|modals
would like|хотел бы|muốn|modals
would like to|хотел бы|muốn|modals
used not to|раньше не|trước không|modals
needn't have|не нужно было|đã không cần|modals
should have|следовало|lẽ ra nên|modals
could have|мог бы|đã có thể|modals
would have|бы (сделал)|đã sẽ|modals
might have|возможно, (сделал)|có lẽ đã|modals
must have|должно быть, (сделал)|chắc hẳn đã|modals
can't have|не мог (сделать)|không thể đã|modals
ought to have|следовало|lẽ ra phải|modals
had better not|лучше не|không nên|modals
would rather not|предпочёл бы не|thà không|modals
be going to|собираться|sắp|modals
"""


def aux_extra() -> list[tuple[str, str, str, str]]:
    rows = []
    for raw in AUX_EXTRA.strip().splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        en, ru, vi, theme = [p.strip() for p in line.split("|")]
        rows.append((en, ru, vi, theme))
    return rows


def slug(text: str) -> str:
    chars = [ch if ch.isalnum() else "_" for ch in text.lower()]
    s = "".join(chars).strip("_")
    while "__" in s:
        s = s.replace("__", "_")
    return s or "x"


def prep_id(en: str) -> str:
    return PREP_IDS.get(en, f"fn.{slug(en)}")

    rows = []
    for raw in AUX_EXTRA.strip().splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        en, ru, vi, theme = [p.strip() for p in line.split("|")]
        rows.append((en, ru, vi, theme))
    return rows
