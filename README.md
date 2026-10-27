# ዲሊጀንት ትምህርት ቤት ማኔጅመንት ሲስተም (Dilijent School MIS)

የኢትዮጵያ ስርዓተ-ትምህርት ደረጃዎችን ያሟላ የትምህርት ቤት መረጃ አስተዳደር፣ የተማሪዎች እና የመምህራን መዝገብ፣ ተለዋዋጭ የውጤት ምዘና፣ የክፍል አላፊ ሮስተር፣ የተማሪዎች ካርድ እና አቴንዳንስ መከታተያ የተሟላ ሲስተም (Android Mobile App + Web Application)።

Comprehensive Ethiopian School Management Information System (MIS) built with Jetpack Compose & modern responsive Web. Features dynamic curriculum assessment schemes (Public & Private school models), distinct Continuous Assessment Mark Lists and Homeroom Rosters (aggregating out of 100), automated printable Report Cards, bidirectional 4-way scrollable tables, and role-based Dual Authentication with Admin Master Password Control.

---

## 🔑 የመግቢያ መለያዎች እና ሚናዎች (Login Credentials & Access Roles)

ሲስተሙ ሁለት ዓይነት የመግቢያ መንገዶችን ያካተተ ሲሆን እያንዳንዱ ተጠቃሚ ወደ ተፈቀደለት ገፅ ብቻ ይገባል፦

### 1. 👑 ሲስተም አድሚን (እርስዎ - Super Admin)
* **የመግቢያ መንገድ:** በኢሜይል እና በይለፍ ቃል ብቻ (Email & Password)
* **ኢሜይል (Email):** `admin@dilijent.edu.et`
* **የይለፍ ቃል (Password):** `admin123`
* **ልዩ ስልጣን:**
  - የመላው ሲስተሙ ዋና ተቆጣጣሪ
  - **🔐 የይለፍ ቃል ማዕከል (Admin Password Master Control):** የሁሉንም ተጠቃሚዎች የይለፍ ቃል መመልከት፣ መደበቅ/ማሳየት እና ወዲያውኑ አዲስ የይለፍ ቃል መቀየር/ማደስ (Reset Password) ይችላል።

### 2. 🏛️ ርዕሰ መምህር (Principal)
* **የመግቢያ መንገድ:** በስልክ ቁጥር እና በይለፍ ቃል (Phone & Password)
* **ስልክ ቁጥር (Phone):** `0911000000`
* **የይለፍ ቃል (Password):** `1234`
* **ስልጣን:** አጠቃላይ የትምህርት ቤቱን እንቅስቃሴ፣ መምህራን፣ ተማሪዎች፣ የሥራ ክፍፍል፣ ሰራተኞች እና የውጤት ምዘና ስርዓትን ያስተዳድራል።

### 3. 👨‍🏫 መምህራን (Teachers & Homeroom Teachers)
* **የመግቢያ መንገድ:** በስልክ ቁጥር እና በይለፍ ቃል
* **ስልክ ቁጥር:** `0911223344` (መምህር ካሳሁን - የ 9A ክፍል አላፊ)
* **የይለፍ ቃል:** `1234`
* **ተግባራት:**
  - **የማያቋርጥ ምዘና ማርክ ሊስት (Mark List):** ለእያንዳንዱ በሚያስተምረው የትምህርት ዓይነት ምዘናዎችን (ፈተና፣ የቤት ሥራ፣ የክፍል ሥራ፣ ፕሮጀክት) ይሞላል።
  - **የክፍል አላፊ ሮስተር (Homeroom Roster):** ለተመደበለት ክፍል ብቻ የሁሉንም ትምህርቶች ድምር ከ100 በመውሰድ አጠቃላይ ሮስተር ያመነጫል።
  - **የተማሪዎች ካርድ (Student Report Card):** በኢትዮጵያ ካሪክለም መስፈርት የተዘጋጀ ኦፊሴላዊ የተማሪዎች ካርድ አዘጋጅቶ በቀጥታ ፕሪንት ያደርጋል።

### 4. 🎓 ተማሪዎች እና ወላጆች (Students & Parents)
* **የመግቢያ መንገድ:** በስልክ ቁጥር እና በይለፍ ቃል
* **ስልክ ቁጥር:** `0944112233` (ተማሪ ናሆም ተስፋዬ)
* **የይለፍ ቃል:** `1234`
* **ተግባራት:** የራስን የግል የትምህርት ውጤት ካርድ እና የዕለት ተዕለት አቴንዳንስ መመልከት።

---

## 🌟 ዋና ዋና አገልግሎቶች እና የተካተቱ ማሻሻያዎች (Key Features)

1. **ማርክ ሊስት እና ሮስተር ተለይተው የተዘጋጁ ናቸው (Mark List vs Roster)**:
   - **ማርክ ሊስት (Mark List):** መምህራን ለሚያስተምሩት የትምህርት አይነት የማያቋርጥ ምዘና (Continuous Assessment) የሚመዘግቡበት ገፅ።
   - **ሮስተር (Roster):** ከማርክ ሊስቱ ጋር ሳይቀላቀል፣ እያንዳንዱ የትምህርት አይነት ከ 100 ድምር ተወስዶ በክፍል አላፊ መምህር ገፅ ብቻ የሚሰላ ዓመታዊ ሰንጠረዥ።
   - **ኦፊሴላዊ የተማሪ ካርድ (Official Report Card):** ከሮስተሩ ተመንጭቶ የሚዘጋጅ ውብና በኢትዮጵያ የትምህርት ሚኒስቴር ስታንዳርድ የተዘጋጀ ካርድ።

2. **በአራቱም አቅጣጫ የሚንሸራተቱ ሰንጠረዦች (Bidirectional 4-Way Scroll)**:
   - ሁሉም የማርክ ሊስት፣ የሮስተር፣ የተማሪዎች እና የመምህራን ሰንጠረዦች በሞባይል ስልክ ስክሪን ወደ ላይ፣ ወደ ታች፣ ወደ ግራ እና ወደ ቀኝ ያለምንም መቆራረጥ ይንሸራተታሉ።

3. **የአድሚን የይለፍ ቃል መቆጣጠሪያ ማዕከል (Admin Master Password Control)**:
   - ሲስተም አድሚን በማንኛውም ጊዜ የማንኛውንም ተጠቃሚ የይለፍ ቃል ማየት፣ ማረጋገጥ እና መለወጥ ይችላል።

4. **ሙሉ ከመስመር ውጭ እና ከክላውድ ጋር የተሳሰረ (Offline-First + Cloud Ready)**:
   - ኢንተርኔት በሌለበት አካባቢም ሆነ በስልክ ላይ ያለ ምንም ችግር በሙሉ አቅሙ ይሰራል።

---

## 🌐 በ GitHub Pages የቀጥታ ዌብሳይት ሊንክ (Live Website Link) ለማግኘት

ይህንን ፕሮጀክት ወደ GitHub ከሰቀሉ በኋላ በ 1 ደቂቃ ውስጥ ነፃ ቋሚ ድረ-ገጽ ለማድረግ የሚከተሉትን ቀላል ደረጃዎች ይከተሉ፦

1. ወደ GitHub ሪፖዚተሪዎ ይሂዱ።
2. የላይኛውን **Settings (⚙️)** ሜኑ ይጫኑ።
3. በግራ በኩል ካሉት አማራጮች **Pages** የሚለውን ይምረጡ።
4. በ **Build and deployment** ስር፦
   * **Source** የሚለውን **Deploy from a branch** ያድርጉት።
   * **Branch:** `main` (ወይም `master`)፣ **Folder:** `/docs` (ወይም `/(root)`) ይምረጡ።
5. **Save** ይበሉት!

🎉 **በ 30 ሰከንድ ውስጥ የራስዎ ቋሚ የትምህርት ቤት ድረ-ገጽ ሊንክ ይሰጥዎታል፦**
```
https://<የእርስዎ-GitHub-Username>.github.io/<የሪፖዚተሪው-ስም>/
```
ይህ ሊንክ በማንኛውም ስልክ፣ ታብሌት እና ኮምፒውተር ላይ ያለ ምንም መተግበሪያ መጫን በቀጥታ ለሁሉም ተማሪዎች፣ ወላጆች እና መምህራን ይከፍታል!

---

## 📲 የአንድሮይድ አፕሊኬሽን (Android Mobile App - APK)

የተሟላውን የአንድሮይድ መተግበሪያ በስልክዎ ላይ በቀጥታ ለመጫን በሪፖዚተሪው ውስጥ የተካተተውን ፋይል ማውረድ ይችላሉ፦
* **`docs/dilijent-school.apk`** ወይም **`public/dilijent-school.apk`**
* እንዲሁም በአይ ስቱዲዮ (AI Studio) የላይኛው ሜኑ **Settings -> Export -> Download APK** በማለት ማውረድ ይችላሉ።
