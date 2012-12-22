class CreateCars < ActiveRecord::Migration
  def change
    create_table :cars do |t|
      t.string :carid
      t.string :name

      t.timestamps
    end

    add_index :cars, :carid
    add_index :cars, :name
  end
end
