package coffee;

public class WhippedCream extends CoffeeDecorator {

    public WhippedCream(Coffee coffee) {
        super(coffee);
    }

    @Override
    public void price() {
        coffee.price();
        System.out.println("With whipped cream price: 3");
    }

    @Override
    public void makeCoffee() {
        coffee.makeCoffee();
        System.out.println("Added topping: Whipped Cream");
    }
}