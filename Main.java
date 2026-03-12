package dogPK;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 创建第一只狗
        System.out.print("请输入第一只小狗的名字：");
        String name1 = scanner.nextLine().trim();
        Profession prof1 = null;
        while (prof1 == null) {
            System.out.print("请选择 " + name1 + " 的职业（法师/战士/刺客）：");
            String profStr = scanner.nextLine().trim();
            prof1 = parseProfession(profStr);
            if (prof1 == null) {
                System.out.println("无效职业，请重新输入！");
            }
        }

        // 创建第二只狗
        System.out.print("\n请输入第二只小狗的名字：");
        String name2 = scanner.nextLine().trim();
        Profession prof2 = null;
        while (prof2 == null) {
            System.out.print("请选择 " + name2 + " 的职业（法师/战士/刺客）：");
            String profStr = scanner.nextLine().trim();
            prof2 = parseProfession(profStr);
            if (prof2 == null) {
                System.out.println("无效职业，请重新输入！");
            }
        }

        // 初始化狗和游戏
        Dog dog1 = new Dog(name1, prof1);
        Dog dog2 = new Dog(name2, prof2);
        Game game = new Game(scanner);
        game.run(dog1, dog2);

        scanner.close();
    }

    // 解析职业字符串为枚举
    private static Profession parseProfession(String s) {
        switch (s.trim()) {
            case "法师": return Profession.MAGE;
            case "战士": return Profession.WARRIOR;
            case "刺客": return Profession.ASSASSIN;
            default: return null;
        }
    }
}