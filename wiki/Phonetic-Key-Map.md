# Phonetic Key Map (ফোনেটিক কি-ম্যাপ)

Bongo implements the standard **Avro Phonetic Typing System**. It maps natural English pronunciations and sounds directly to pure Bengali script.

---

## Vowels (স্বরবর্ণ)

When typed at the beginning of a word or after another vowel, these keystrokes produce independent vowel letters. When typed after a consonant, they automatically convert to vowel diacritics (কার).

| English Keystroke | Independent Vowel (পূর্ণ বর্ণ) | Diacritic (কার) | Example Keystroke | Example Output | Meaning |
|:---:|:---:|:---:|:---|:---|:---|
| `o` | **অ** | *(inherent sound)* | `onek` | অনেক | Many |
| `a` | **আ** | **া** | `ami` | আমি | I / Me |
| `i` | **ই** | **ি** | `iti` | ইতি | Yours / End |
| `I` / `ee` | **ঈ** | **ী** | `Ishor` | ঈশ্বর | God |
| `u` | **উ** | **ু** | `uth` | উঠ | Get up |
| `U` / `oo` | **ঊ** | **ূ** | `Ushor` | ঊষর | Barren |
| `rri` | **ঋ** | **ৃ** | `rrishi` | ঋষি | Sage |
| `e` | **এ** | **ে** | `ektara` | একতারা | Musical instrument |
| `OI` | **ঐ** | **ৈ** | `OIkyoboddho` | ঐক্যবদ্ধ | United |
| `O` | **ও** | **ো** | `Ol` | ওল | Yam |
| `OU` | **ঔ** | **ৌ** | `OUshodh` | ঔষধ | Medicine |

---

## Consonants (ব্যঞ্জনবর্ণ)

| Keystroke | Bengali Glyph | Example Keystroke | Example Output | Meaning |
|:---:|:---:|:---|:---|:---|
| `k` | **ক** | `kothay` | কোথায় | Where |
| `kh` | **খ** | `khobor` | খবর | News |
| `g` | **গ** | `gan` | গান | Song |
| `gh` | **ঘ** | `ghor` | ঘর | House / Room |
| `Ng` | **ঙ** | `roNg` | রং | Color |
| `c` / `ch` | **চ** | `chad` | চাঁদ | Moon |
| `Ch` | **ছ** | `Chobi` | ছবি | Picture |
| `j` | **জ** | `jol` | জল | Water |
| `jh` | **ঝ** | `jhor` | ঝড় | Storm |
| `NG` | **ঞ** | `miya` | মিঞা | Title / Surname |
| `T` | **ট** | `Taka` | টাকা | Money |
| `Th` | **ঠ** | `Thik` | ঠিক | Correct |
| `D` | **ড** | `Daak` | ডাক | Call / Mail |
| `Dh` | **ঢ** | `Dhak` | ঢাক | Drum |
| `N` | **ণ** | `gun` | গুণ | Quality |
| `t` | **ত** | `tumi` | তুমি | You |
| `th` | **থ** | `thako` | থাকো | Stay |
| `d` | **দ** | `din` | দিন | Day |
| `dh` | **ধ** | `dhon` | ধন | Wealth |
| `n` | **ন** | `naam` | নাম | Name |
| `p` | **প** | `pani` | পানি | Water |
| `f` / `ph` | **ফ** | `ful` | ফুল | Flower |
| `b` | **ব** | `boi` | বই | Book |
| `bh` / `v` | **ভ** | `bhalobasha` | ভালোবাসা | Love |
| `m` | **ম** | `manush` | মানুষ | Human |
| `z` | **য** | `jao` | যাও | Go |
| `r` | **র** | `raat` | রাত | Night |
| `l` | **ল** | `lal` | লাল | Red |
| `sh` / `S` | **শ** | `shobuj` | সবুজ | Green |
| `Sh` | **ষ** | `koShTo` | কষ্ট | Trouble |
| `s` | **স** | `shokal` | সকাল | Morning |
| `h` | **হ** | `haat` | হাত | Hand |
| `R` | **ড়** | `paRha` | পড়া | Study |
| `Rh` | **ঢ়** | `aaRha` | আঢ়া | Half |
| `y` | **য়** | `shomoy` | সময় | Time |
| `t`` | **ৎ** | `hothat` | হঠাৎ | Suddenly |
| `ng` | **ং** | `ongko` | অংক | Math |
| `:` | **ঃ** | `du:kho` | দুঃখ | Sadness |
| `^` | **ঁ** | `c^ad` | চাঁদ | Moon |

---

## Conjunct Rules (যুক্তবর্ণ)

Bongo forms conjunct characters seamlessly when typing consecutive consonants:

### 1. Natural Conjuncts
Typing consonants together naturally blends them according to standard Bengali orthography:
- `kkh` → **ক্ষ** (e.g. `kkhoma` → ক্ষমা)
- `kkhN` → **ক্ষ্ণ** (e.g. `tikkhoN` → তীক্ষ্ণ)
- `kkhm` → **ক্ষ্ম** (e.g. `shukkho` → সূক্ষ্ম)
- `gn` → **জ্ঞ** (e.g. `gyan` → জ্ঞান)
- `ngg` → **ঙ্গ** (e.g. `bongo` → বঙ্গ)
- `nch` → **ঞ্চ** (e.g. `ponchom` → পঞ্চম)
- `nj` → **ঞ্জ** (e.g. `gonj` → গঞ্জ)
- `ND` → **ণ্ড** (e.g. `thanDa` → ঠান্ডা)
- `nt` → **ন্ত** (e.g. `shanto` → শান্ত)
- `nth` → **ন্থ** (e.g. `grontho` → গ্রন্থ)
- `nd` → **ন্দ** (e.g. `anondo` → আনন্দ)
- `ndh` → **ন্ধ** (e.g. `ondho` → অন্ধ)
- `mp` → **ম্প** (e.g. `sompod` → সম্পদ)
- `mb` → **ম্ব** (e.g. `kombul` → কম্বল)
- `mbh` → **ম্ভ** (e.g. `somvob` → সম্ভব)
- `shch` → **শ্চ** (e.g. `ashchorjo` → আশ্চর্য)
- `ShT` → **ষ্ট** (e.g. `miShTi` → মিষ্টি)
- `st` → **স্ত** (e.g. `rasta` → রাস্তা)

### 2. Special Modifiers (ফলা ও চিহ্ন)

| Modifier | Keystroke Rule | Example | Result |
|:---|:---|:---|:---|
| **য-ফলা (্য)** | Type `y` or `z` after a consonant | `bakkya` | বাক্য |
| **র-ফলা (্র)** | Type `r` after a consonant | `gram` | গ্রাম |
| **রেফ (র্)** | Type `rr` before a consonant | `korma` | কর্ম |
| **ব-ফলা (্ব)** | Type `w` or `b` after a consonant | `shadhin` (or `shwadhin`) | স্বাধীন |
| **হসন্ত (্)** | Type double comma `,,` to force a pure halant | `d,,r` | দ্​র |
| **চন্দ্রবিন্দু (ঁ)** | Type `^` after a vowel or consonant | `h^ash` | হাঁস |

---

## Numbers & Special Characters

| Keystroke | Bengali Output |
|:---:|:---:|
| `0` | **০** |
| `1` | **১** |
| `2` | **২** |
| `3` | **৩** |
| `4` | **৪** |
| `5` | **৫** |
| `6` | **৬** |
| `7` | **৭** |
| `8` | **৮** |
| `9` | **৯** |
| `.` (in sentence) | **।** (দাঁড়ি) |
| `..` | **॥** (ডাবল দাঁড়ি) |
| `$` | **৳** (টাকা চিহ্ন) |

---

## Helpful Typing Tips

1. **Case Sensitivity**:
   Uppercase letters map to retroflex or distinct characters:
   - `t` gives **ত**, while `T` gives **ট**.
   - `d` gives **দ**, while `D` gives **ড**.
   - `n` gives **ন**, while `N` gives **ণ**.
   - `s` gives **স**, `sh` gives **শ**, and `Sh` gives **ষ**.

2. **Smart Autocorrect**:
   Bongo automatically recommends proper spelling suggestions in the candidate HUD window if a word is spelled slightly differently. You can hit **Space** to accept the default word, or press `[1]`, `[2]`, `[3]` to pick an alternate choice immediately.
