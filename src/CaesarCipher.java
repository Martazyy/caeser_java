import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

//Инициализация констант - алфавитов
public class CaesarCipher {
    private static final String RUSSIAN_UPPER = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
    private static final String RUSSIAN_LOWER = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    private static final String ENGLISH_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ENGLISH_LOWER = "abcdefghijklmnopqrstuvwxyz";

    // Метод для сдвига символа в пределах алфавита
    private static char shiftChar(char c, int shift, String alphabet) {
        int index = alphabet.indexOf(c);
        if (index == -1) {
            return c; // Если символ не найден в алфавите, возвращаем его без изменений
        }
        int newIndex = (index + shift) % alphabet.length(); // Обеспечиваем корректный сдвиг
        if (newIndex < 0) { // Если индекс стал отрицательным, корректируем его
            newIndex += alphabet.length();
        }
        return alphabet.charAt(newIndex);
    }

    // Метод для шифрования и дешифрования (общий для всех алфавитов)
    private static String shiftText(String text, int shift, String alphabet) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (alphabet.indexOf(c) != -1) {
                result.append(shiftChar(c, shift, alphabet));
            } else {
                result.append(c); // Для символов, не входящих в алфавит
            }
        }
        return result.toString();
    }

    // Шифрование текст
    public static String encrypt(String text, int shift) {
        StringBuilder result = new StringBuilder();
        // Шифруем текст по всем алфавитам в одном цикле
        for (char c : text.toCharArray()) {
            if (RUSSIAN_UPPER.indexOf(c) != -1) {
                result.append(shiftChar(c, shift, RUSSIAN_UPPER)); // Обрабатываем заглавные русские буквы
            } else if (RUSSIAN_LOWER.indexOf(c) != -1) {
                result.append(shiftChar(c, shift, RUSSIAN_LOWER)); // Обрабатываем строчные русские буквы
            } else if (ENGLISH_UPPER.indexOf(c) != -1) {
                result.append(shiftChar(c, shift, ENGLISH_UPPER)); // Обрабатываем заглавные английские буквы
            } else if (ENGLISH_LOWER.indexOf(c) != -1) {
                result.append(shiftChar(c, shift, ENGLISH_LOWER)); // Обрабатываем строчные английские буквы
            } else {
                result.append(c); // Оставляем символ без изменений (например, пробелы или знаки препинания)
            }
        }
        return result.toString();
    }

    //Дешифрование текста
    public static String decrypt(String text, int shift) {
        return encrypt(text, -shift);
    }

    // Обработка файлов для шифрования и дешифрования с известным сдвигом
    public static void processFile(String inputPath, String outputPath, int shift, boolean decrypt) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(inputPath)));
        String result = decrypt ? decrypt(content, shift) : encrypt(content, shift);
        Files.write(Paths.get(outputPath), result.getBytes());
    }

    // Дешифрование методом brute force
    public static void bruteForceDecrypt(String inputPath, String examplePath, String outputPath) throws IOException {
        if (!Files.exists(Paths.get(inputPath))) {
            throw new FileNotFoundException("Файл " + inputPath + " не найден.");
        }

        String content = new String(Files.readAllBytes(Paths.get(inputPath)));
        String example = examplePath != null && Files.exists(Paths.get(examplePath)) ? new String(Files.readAllBytes(Paths.get(examplePath))) : "";

        for (int shift = 1; shift <= Math.max(RUSSIAN_UPPER.length(), ENGLISH_UPPER.length()); shift++) {
            String decrypted = decrypt(content, shift);
            // Проверка на начало текста (сравнение первых 100 символов)
            if (!example.isEmpty() && decrypted.contains(example.substring(0, Math.min(100, example.length())))) {
                Files.write(Paths.get(outputPath), decrypted.getBytes());
                System.out.println("Успешно расшифровано с сдвигом: " + shift);
                return;
            }
        }
        System.out.println("Не удалось автоматически определить сдвиг. Проверьте вручную.");
    }

    // Дешифрование методом частотного анализа
    public static int frequencyAnalysisDecrypt(String inputPath) throws IOException {
        if (!Files.exists(Paths.get(inputPath))) {
            throw new FileNotFoundException("Файл " + inputPath + " не найден.");
        }

        String content = new String(Files.readAllBytes(Paths.get(inputPath)));
        Map<Character, Integer> frequencyMap = new HashMap<>();
        for (char c : content.toCharArray()) {
            if (Character.isLetter(c)) {
                frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
            }
        }
        // Поиск наиболее часто встречающегося символа
        char mostCommon = frequencyMap.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
        int shift = (RUSSIAN_LOWER.indexOf(mostCommon) - RUSSIAN_LOWER.indexOf('о') + RUSSIAN_LOWER.length()) % RUSSIAN_LOWER.length();
        return shift;
    }

    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.println("Выберите режим работы:");
                System.out.println("1: Шифрование");
                System.out.println("2: Дешифрование с известным ключом");
                System.out.println("3: Дешифрование brute force");
                System.out.println("4: Дешифрование методом частотного анализа");
                System.out.println("5: Выход");
                String choice = reader.readLine();

                if (choice.equals("1") || choice.equals("2")) {
                    System.out.println("Введите путь к входному файлу:");
                    String inputPath = reader.readLine();
                    System.out.println("Введите путь к выходному файлу:");
                    String outputPath = reader.readLine();
                    System.out.println("Введите сдвиг (ключ):");
                    int shift = Integer.parseInt(reader.readLine());
                    processFile(inputPath, outputPath, shift, choice.equals("2"));
                } else if (choice.equals("3")) {
                    System.out.println("Введите путь к входному файлу:");
                    String inputPath = reader.readLine();
                    System.out.println("Введите путь к файлу с примером текста:");
                    String examplePath = reader.readLine();
                    System.out.println("Введите путь к выходному файлу:");
                    String outputPath = reader.readLine();
                    bruteForceDecrypt(inputPath, examplePath, outputPath);
                } else if (choice.equals("4")) {
                    System.out.println("Введите путь к входному файлу:");
                    String inputPath = reader.readLine();
                    int shift = frequencyAnalysisDecrypt(inputPath);
                    System.out.println("Предполагаемый сдвиг: " + shift);
                } else if (choice.equals("5")) {
                    System.out.println("Выход из программы.");
                    break;
                } else {
                    System.out.println("Неверный ввод. Попробуйте снова.");
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}

// 1 и 2
//Введите путь к входному файлу:
//example.txt
//Введите путь к файлу с примером текста:
//*.txt


// 3 и 4
//Введите путь к входному файлу:
//output.txt
//Введите путь к файлу с примером текста:
//example.txt
//Введите путь к выходному файлу:
//input.txt
