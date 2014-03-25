Sequel.migration do
  up do
    alter_table(:users) do
      add_column :name, String, :size => 200
    end
  end

  down do
    alter_table(:users) do
      drop_column :name
    end
  end
end
