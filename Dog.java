package dogPK;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Dog {
    private String name;
    private int health;
    private Profession profession;
    private int defense;
    private List<String> skills;
    private Map<String, Integer> skillCD;
    private boolean canAttack;
    private double damageReduction; // 自身减伤倍率（1.0=无减伤，0.7=减伤30%）
    private int poisonTurns; // 缠绕中毒回合数
    private boolean hasUsedSteal; // 是否使用过偷梁换柱
    private boolean dodgeTriggered; // 本局是否触发躲闪

    public Dog(String name, Profession profession) {
        this.name = name;
        this.health = 100; // 初始血量固定100
        this.profession = profession;
        this.canAttack = true;
        this.damageReduction = 1.0;
        this.poisonTurns = 0;
        this.hasUsedSteal = false;
        this.dodgeTriggered = false;
        this.skillCD = new HashMap<>();
        this.skills = new ArrayList<>();

        // 初始化职业属性和技能
        switch (profession) {
            case MAGE:
                this.defense = 35;
                this.skills.add("吸血");
                this.skills.add("魔法攻击");
                this.skills.add("缠绕");
                // 初始化所有技能CD（0=无CD）
                this.skillCD.put("吸血", 0);
                this.skillCD.put("魔法攻击", 0);
                this.skillCD.put("缠绕", 0);
                break;
            case WARRIOR:
                this.defense = 70;
                this.skills.add("虎躯一震");
                this.skills.add("战吼");
                this.skills.add("剑气");
                this.skillCD.put("虎躯一震", 0);
                this.skillCD.put("战吼", 0);
                this.skillCD.put("剑气", 0);
                break;
            case ASSASSIN:
                this.defense = 55;
                this.skills.add("躲闪");
                this.skills.add("偷梁换柱");
                this.skills.add("暴击");
                this.skillCD.put("躲闪", 0);
                this.skillCD.put("偷梁换柱", 0);
                this.skillCD.put("暴击", 0);
                break;
        }
    }

    // 执行单个技能，返回技能造成的基础伤害（未计算防御/减伤）
    public int performSkill(String skillName, Dog target) {
        int skillDamage = 0;
        // 检查CD
        if (skillCD.getOrDefault(skillName, 0) > 0) {
            System.out.println(name + " 的" + skillName + "还在CD中，无法使用！");
            return 0;
        }

        switch (skillName) {
            case "吸血":
                // 吸血：转移对方剩余血量15%（改为返回伤害值，统一走takeDamage计算）
                int stealHp = (int) (target.getHealth() * 0.12);
                skillDamage = stealHp; // 记录技能伤害
                // 后续在Game中统一处理血量转移，避免直接修改
                break;
            case "魔法攻击":
                skillDamage = 22; // 固定25点伤害
                this.skillCD.put("魔法攻击", 1); // 本局CD
                break;
            case "缠绕":
                target.setPoisonTurns(5); // 接下来5局每局掉5血
                this.skillCD.put("缠绕", 1); // 本局CD
                break;
            case "虎躯一震":
                skillDamage = 10; // 固定10点伤害
                target.setCanAttack(false); // 对方下一局无法攻击
                this.skillCD.put("虎躯一震", 1); // 本局CD
                break;
            case "战吼":
                // 自身血量增加剩余血量20%
                int healHp = (int) (this.health * 0.12);
                this.health += healHp;
                System.out.println(name + " 战吼回血：" + healHp + "，当前血量：" + this.health);
                // 自身下一局减伤30%
                this.damageReduction = 0.7;
                this.skillCD.put("战吼", 1); // 本局CD
                break;
            case "剑气":
                // 二连击：12% → 25%（基于当前血量）
                int firstHit = (int) (target.getHealth() * 0.09);
                int secondHit = (int) ((target.getHealth() - firstHit) * 0.18);
                skillDamage = firstHit + secondHit;
                this.skillCD.put("剑气", 1); // 本局CD
                break;
            case "躲闪":
                // 标记躲闪触发状态（在被攻击时生效）
                Random random = new Random();
                if (random.nextDouble() < 0.5) {
                    this.dodgeTriggered = true;
                    System.out.println(name + " 触发躲闪！本局免疫所有伤害");
                } else {
                    System.out.println(name + " 躲闪失败，无效果");
                }
                break;
            case "偷梁换柱":
                if (!this.hasUsedSteal) {
                    Random random1 = new Random();
                    // 随机窃取对方一个技能
                    String stolenSkill = target.getSkills().get(random1.nextInt(target.getSkills().size()));
                    // 避免重复添加
                    if (!this.skills.contains(stolenSkill)) { // 只有当未拥有该技能时才添加
                        this.skills.add(stolenSkill);
                        this.skillCD.put(stolenSkill, 0); // 初始化窃取技能的CD
                        System.out.println(name + " 窃取了" + target.getName() + "的技能：" + stolenSkill);
                    }
                    // 立即使用窃取的技能
                    skillDamage += this.performSkill(stolenSkill, target);
                    this.hasUsedSteal = true; // 标记已使用
                    this.skillCD.put("偷梁换柱", Integer.MAX_VALUE); // 永久CD
                } else {
                    System.out.println(name + " 偷梁换柱只能使用一次！");
                }
                break;
            case "暴击":
                skillDamage = (int) (target.getHealth() * 0.5); // 对方剩余血量50%
                this.skillCD.put("暴击", 2); // CD 2局
                break;
            default:
                System.out.println("未知技能：" + skillName);
                break;
        }
        return skillDamage;
    }

    // 计算实际受到的伤害（防御+减伤+躲闪）
    public int calculateActualDamage(int totalAttack) {
        if (this.dodgeTriggered) {
            this.dodgeTriggered = false; // 重置躲闪状态
            return 0;
        }
        // 基础减伤：防御值*50%
        double defenseReduction = this.defense * 0.5;
        double actualDamage = totalAttack - defenseReduction;
        // 最低伤害为0
        if (actualDamage < 0) actualDamage = 0;
        // 应用自身减伤倍率（如战吼的70%）
        actualDamage *= this.damageReduction;
        // 重置减伤倍率（仅生效一局）
        this.damageReduction = 1.0;
        return (int) actualDamage;
    }

    // 受到伤害（最终血量不低于0）
    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health < 0) this.health = 0;
    }

    // 处理吸血效果（单独抽离，避免直接修改血量）
    public void applyVampire(Dog target, int stealHp) {
        target.takeDamage(stealHp); // 目标掉血
        this.health += stealHp; // 自身回血
        System.out.println(name + " 吸血：从" + target.getName() + "吸取" + stealHp + "血量");
    }

    // 回合结束：更新CD、处理中毒、重置状态
    public void endTurn() {
        // 更新CD
        for (String skill : skillCD.keySet()) {
            if (skillCD.get(skill) > 0) {
                skillCD.put(skill, skillCD.get(skill) - 1);
            }
        }
        // 处理缠绕中毒
        applyPoison();
        // 重置躲闪状态
        this.dodgeTriggered = false;
    }

    // 应用中毒伤害
    public void applyPoison() {
        if (this.poisonTurns > 0) {
            int poisonDamage = 5;
            this.health -= poisonDamage;
            if (this.health < 0) this.health = 0;
            this.poisonTurns--;
            System.out.println(name + " 受到缠绕中毒伤害：" + poisonDamage + "，剩余中毒回合：" + this.poisonTurns);
        }
    }

    // Getter & Setter
    public String getName() { return name; }
    public int getHealth() { return health; }
    public Profession getProfession() { return profession; }
    public int getDefense() { return defense; }
    public boolean isAlive() { return health > 0; }
    public boolean canAttack() { return canAttack; }
    public void setCanAttack(boolean canAttack) { this.canAttack = canAttack; }
    public int getNormalAttackDamage() {
        switch (profession) {
            case MAGE: return 13;
            case WARRIOR: return 6;
            case ASSASSIN: return 9;
            default: return 0;
        }
    }
    public List<String> getSkills() { return skills; }
    public int getPoisonTurns() { return poisonTurns; }
    public void setPoisonTurns(int poisonTurns) { this.poisonTurns = poisonTurns; }
    public boolean isDodgeTriggered() { return dodgeTriggered; }
}