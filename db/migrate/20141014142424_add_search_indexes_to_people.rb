Sequel.migration do
  up do
    alter_table(:people) do
      add_index :first_name
      add_index :last_name
      add_index :email
    end
  end

  down do
    alter_table(:people) do
      drop_index :first_name
      drop_index :last_name
      drop_index :email
    end
  end
end
