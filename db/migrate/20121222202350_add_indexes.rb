class AddIndexes < ActiveRecord::Migration
  def up
  	add_index :positions, :created_at
  	add_index :positions, :name
  end

  def down
  	remove_index :positions, :created_at
  	remove_index :positions, :name
  end
end
